package org.sunyaxing.imagine.jdataviewapi.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

@Data
@NoArgsConstructor
public class ThreadSpace implements Serializable {
    public static final Logger LOGGER = LoggerFactory.getLogger(ThreadSpace.class);
    // 线程相关
    private Long threadId;
    private String threadName;
    private Long stepIndex;
    // 类与方法栈
    private String className;
    private String methodName;
    // 方法调用的开始时间
    private Long methodStartTime;
    // 当前方法的状态
    private LifeCycle.MethodState methodState;
    // 方法调用的结束时间
    private Long methodEndTime;
    // 方法调用深度
    private Long depth;

    public ThreadSpace(String className, Method method) {
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.stepIndex = 0L;

        this.className = className;
        this.methodName = method.getName();
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
