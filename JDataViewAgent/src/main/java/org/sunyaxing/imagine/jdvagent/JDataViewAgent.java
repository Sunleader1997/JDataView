package org.sunyaxing.imagine.jdvagent;

import com.alibaba.fastjson2.JSONObject;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdvagent.advices.ProfilingAdvice;
import org.sunyaxing.imagine.jdvagent.cache.OrgClassByteManager;
import org.sunyaxing.imagine.jdvagent.dicts.LogDicts;
import org.sunyaxing.imagine.jdvagent.sender.base.JDataViewWebSocketClient;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

public class JDataViewAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewAgent.class);
    private static ClassFileTransformer classFileTransformerOnInstall;
    private static Instrumentation instrumentationOnInstall;
    public static ConfigProperties configProperties;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
    }

    /**
     * 按照;分割
     *
     * @param agentArgs key=value;key2=value2
     */
    private static void parseArgs(String agentArgs) {
        String[] args = agentArgs.split(";");
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length != 2) {
                continue;
            }
            map.put(kv[0], kv[1]);
        }
        configProperties = JSONObject.parseObject(JSONObject.toJSONString(map), ConfigProperties.class);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        parseArgs(agentArgs);
        configProperties.valid();
        switch (configProperties.getMode()) {
            // 如果是安装模式，需要判断是否已经安装，如果已经安装，重新创建客户端即可，class不需要重载
            case "install" -> {
                JDataViewWebSocketClient.reCreate();
                if (!configProperties.isAttached()) {
                    install(instrumentation);
                }
                configProperties.setAttached(true);
            }
            // 如果是卸载模式，需要恢复class,断开客户端,设置attached为false
            case "uninstall" -> {
                try {
                    instrumentationOnInstall.removeTransformer(classFileTransformerOnInstall);
                    JDataViewWebSocketClient.INSTANCE.close();
                    OrgClassByteManager.restore(instrumentationOnInstall);
                    configProperties.setAttached(false);
                } catch (Exception e) {
                    LOGGER.error(LogDicts.LOG_PREFIX + "卸载异常", e);
                }
            }
        }
    }

    public static void install(Instrumentation instrumentation) {
        instrumentationOnInstall = instrumentation;
        classFileTransformerOnInstall = new AgentBuilder.Default()
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(getElementMatcher())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                    // 备份原始字节码
                    OrgClassByteManager.put(typeDescription.getName(), classLoader, builder.make().getBytes());
                    DynamicType.Builder<?> dynamicType = builder
                            .visit(Advice.to(ProfilingAdvice.class).on(
                                    ElementMatchers.isMethod()
                                            .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                            ));
                    return dynamicType;
                }).installOn(instrumentation);
    }

    private static ElementMatcher.Junction<TypeDescription> getElementMatcher() {
        return ElementMatchers.nameStartsWith(configProperties.getScanPack())
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdvagent")))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdataviewapi")))
                .and(ElementMatchers.not(ElementMatchers.isInterface()));
    }
}
