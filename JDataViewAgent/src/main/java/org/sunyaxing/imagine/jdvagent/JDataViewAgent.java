package org.sunyaxing.imagine.jdvagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.ClassRegistryMsg;
import org.sunyaxing.imagine.jdvagent.advices.ProfilingAdvice;
import org.sunyaxing.imagine.jdvagent.sender.base.JDataViewWebSocketClient;
import org.sunyaxing.imagine.jdvagent.sender.base.Sender;

import java.lang.instrument.Instrumentation;
import java.util.List;

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
                .type(ElementMatchers
                        .nameStartsWith("org.sunyaxing.imagine.jdataview")
                        .and(ElementMatchers.not(
                                ElementMatchers.nameStartsWith("org.sunyaxing.imagine.jdataviewapi")
                        ))
                )
                .transform((builder, typeDescription, classLoader, module) -> {
                    registryToServer(typeDescription);
                    DynamicType.Builder<?> dynamicType = builder
                            .method(ElementMatchers.any())
                            .intercept(Advice.to(ProfilingAdvice.class));
                    return dynamicType;
                }).installOn(instrumentation);
    }

    /**
     * 注册到分析服务
     */
    private static void registryToServer(TypeDescription typeDescription) {
        if (!JDataViewWebSocketClient.isConnected()) return;
        List<String> methodNames = typeDescription.getDeclaredMethods().stream().map(method -> {
            String[] parameterTypes = method.getParameters().stream().map(Object::toString).toArray(String[]::new);
            return method.getReturnType() + " " + method.getName() + "(" + String.join(",", parameterTypes) + ")";
        }).toList();
        ClassRegistryMsg classRegistryMsg = ClassRegistryMsg.builder()
                .className(typeDescription.getName())
                .methodNames(methodNames)
                .build();
        Sender.INSTANCE.send(classRegistryMsg);
    }

    public static void createClient(String agentArgs) {
        // TODO 根据参数初始化CLIENT
        JDataViewWebSocketClient.getInstance();
    }
}
