package org.sunyaxing.imagine.jdvagent;

import com.alibaba.fastjson2.JSONObject;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
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
        JDataViewAgent.configProperties = validArgs(agentArgs);
        createClient(agentArgs);
        install(instrumentation);
    }

    /**
     * 按照;分割
     *
     * @param agentArgs key=value;key2=value2
     */
    private static ConfigProperties validArgs(String agentArgs) {
        String[] args = agentArgs.split(";");
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length != 2) {
                throw new IllegalArgumentException("参数格式错误：" + arg);
            }
            map.put(kv[0], kv[1]);
        }
        ConfigProperties configProperties = JSONObject.parseObject(JSONObject.toJSONString(map), ConfigProperties.class);
        configProperties.valid();
        return configProperties;
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        install(instrumentation);
    }

    public static void install(Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(ElementMatchers
                        .nameStartsWith(JDataViewAgent.configProperties.getScanPack())
                        .and(ElementMatchers.not(
                                ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdataviewapi")
                        ))
                )
                .transform((builder, typeDescription, classLoader, module) -> {
                    DynamicType.Builder<?> dynamicType = builder
                            .method(ElementMatchers.any())
                            .intercept(Advice.to(ProfilingAdvice.class));
                    return dynamicType;
                }).installOn(instrumentation);
    }

    public static void createClient(String agentArgs) {
        // TODO 根据参数初始化CLIENT
        JDataViewWebSocketClient.getInstance();
    }
}
