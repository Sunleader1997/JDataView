package org.sunyaxing.imagine.jdvagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdvagent.advices.ProfilingAdvice;
import org.sunyaxing.imagine.jdvagent.sender.JDataViewWebSocketClient;

import java.lang.instrument.Instrumentation;

public class JDataViewAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewAgent.class);

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        createClient(agentArgs);
        install(instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        install(instrumentation);
    }

    public static void install(Instrumentation instrumentation) {
        new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdataview"))
                .transform((builder, typeDescription, classLoader, module) -> {
                    LOGGER.info("【JDataViewAgent】 获取到类 " + typeDescription.getName());
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
