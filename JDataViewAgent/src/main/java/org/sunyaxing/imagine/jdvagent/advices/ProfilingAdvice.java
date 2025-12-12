package org.sunyaxing.imagine.jdvagent.advices;

import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class ProfilingAdvice {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfilingAdvice.class);

    public static final LinkedHashMap<String, MethodStats> STATS = new LinkedHashMap<>();

    @Advice.OnMethodEnter
    public static long enter(
            @Advice.This Object obj,
            @Advice.Origin Method method) {
        MethodStats methodStats = getMethodStats(obj, method);
        methodStats.addInvocation();
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit
    public static void exit(
            @Advice.This Object obj,
            @Advice.Enter long start,
            @Advice.Origin Method method,
            @Advice.Return Object ret,
            @Advice.AllArguments Object[] args) {
        long current = System.currentTimeMillis();
        long cost = current - start;
        MethodStats methodStats = getMethodStats(obj, method);
        methodStats.addTime(cost);
        LOGGER.info("【JDataViewAgent】 {}", methodStats);
    }

    /**
     * 获取方法统计信息
     */
    public static MethodStats getMethodStats(Object obj, Method method) {
        String classNameAndMethod = obj.getClass().getName() + "." + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
        boolean exist = STATS.containsKey(classNameAndMethod);
        if (!exist) {
            STATS.put(classNameAndMethod, new MethodStats(classNameAndMethod));
        }
        return STATS.get(classNameAndMethod);
    }

    public static class MethodStats {
        private final String name;
        // 总耗时
        private long totalTimeMs;
        // 调用次数
        private long invocationCount;

        public MethodStats(String name) {
            this.name = name;
            this.totalTimeMs = 0;
            this.invocationCount = 0;
        }

        public void addInvocation() {
            this.invocationCount++;
        }

        public void addTime(long durationMs) {
            this.totalTimeMs += durationMs;
        }

        public long getTotalTimeMs() {
            return totalTimeMs;
        }

        public long getInvocationCount() {
            return invocationCount;
        }

        @Override
        public String toString() {
            return "MethodStats{" +
                    "name='" + name + '\'' +
                    ", totalTimeMs=" + totalTimeMs +
                    ", invocationCount=" + invocationCount +
                    '}';
        }
    }
}
