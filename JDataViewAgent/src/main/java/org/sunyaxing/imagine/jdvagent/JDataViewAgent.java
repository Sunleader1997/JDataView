package org.sunyaxing.imagine.jdvagent;

import com.alibaba.fastjson2.JSONObject;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdvagent.advices.ProfilingAdvice;
import org.sunyaxing.imagine.jdvagent.sender.base.JDataViewWebSocketClient;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

public class JDataViewAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewAgent.class);
    public static ConfigProperties configProperties;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        parseArgs(agentArgs);
        configProperties.valid();
        createClient(agentArgs);
        install(instrumentation);
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
        createClient(agentArgs);
        install(instrumentation);
    }

    public static void install(Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(getElementMatcher())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                    System.out.println("JDataViewAgent install " + typeDescription);
                    DynamicType.Builder<?> dynamicType = builder
                            .visit(Advice.to(ProfilingAdvice.class).on(
                                    ElementMatchers.isMethod()
                                            .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                            .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                            .and(ElementMatchers.not(ElementMatchers.nameStartsWith("get")))
                                            .and(ElementMatchers.not(ElementMatchers.nameStartsWith("set")))
                            ));
                    return dynamicType;
                }).installOn(instrumentation);
    }

    private static ElementMatcher.Junction<TypeDescription> getElementMatcher() {
        return ElementMatchers.nameStartsWith(configProperties.getScanPack())
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdvagent")))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdataviewapi")))
                .and(ElementMatchers.not(ElementMatchers.nameContains("$")))
                .and(ElementMatchers.not(ElementMatchers.isInterface()));
    }

    public static void createClient(String agentArgs) {
        // TODO 根据参数初始化CLIENT
        JDataViewWebSocketClient.getInstance();
    }
}
