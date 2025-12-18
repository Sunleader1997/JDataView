package org.sunyaxing.imagine.jdvagent.advices;

import java.util.concurrent.atomic.AtomicLong;

public class Counter {
    // 记录接口调用深度
    private static final ThreadLocal<AtomicLong> depthHolder = ThreadLocal.withInitial(() -> new AtomicLong(0));

    public static long enter() {
        return depthHolder.get().getAndIncrement();
    }

    public static long end() {
        long now = depthHolder.get().decrementAndGet();
        // 防止内存泄露
        if (now == 0) {
            depthHolder.remove();
        }
        return now;
    }
}
