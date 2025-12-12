package org.sunyaxing.imagine.jdataviewapi.data;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class ThreadSpace implements Serializable {
    public static final Logger LOGGER = LoggerFactory.getLogger(ThreadSpace.class);
    // 线程相关
    private final long threadId;
    private final String threadName;
    private final AtomicLong stepIndex;
    // 类与方法栈
    private final Class<?> aClass;
    private final Method method;
    // 方法调用的开始时间
    private final long methodStartTime;
    // 当前方法的状态
    private LifeCycle.MethodState methodState;
    // 方法调用的结束时间
    private long methodEndTime;

    public ThreadSpace(Class<?> aClass, Method method) {
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.stepIndex = new AtomicLong(0);

        this.aClass = aClass;
        this.method = method;
        this.methodStartTime = System.currentTimeMillis();
        this.methodState = LifeCycle.MethodState.ENTER;
    }

    /**
     * 标记方法结束
     */
    public void end(boolean isException) {
        this.methodEndTime = System.currentTimeMillis();
        this.methodState = isException ? LifeCycle.MethodState.EXCEPTION : LifeCycle.MethodState.SUC;
    }

}
