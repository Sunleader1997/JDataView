package org.sunyaxing.imagine.jdvagent.advices;

import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdvagent.sender.base.Sender;

import java.lang.reflect.Method;

/**
 * 方法调用栈采集器
 */
public class ProfilingAdvice {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfilingAdvice.class);

    /**
     * 方法进入时的埋点
     */
    @Advice.OnMethodEnter
    public static ThreadSpace enter(
            @Advice.This Object obj,
            @Advice.Origin Method method,
            @Advice.AllArguments Object[] args) {
        final ThreadSpace threadSpace = new ThreadSpace(obj.getClass(), method);
        threadSpace.setDepth(Counter.enter());
        Sender.INSTANCE.send(threadSpace);
        return threadSpace;
    }

    /**
     * 方法退出时的埋点
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.This Object obj,
            @Advice.Enter ThreadSpace threadSpace,
            @Advice.Origin Method method,
            @Advice.Return Object ret,
            @Advice.Thrown Throwable throwable,
            @Advice.AllArguments Object[] args) {
        threadSpace.end(false);
        threadSpace.setDepth(Counter.end());
        Sender.INSTANCE.send(threadSpace);
    }

}
