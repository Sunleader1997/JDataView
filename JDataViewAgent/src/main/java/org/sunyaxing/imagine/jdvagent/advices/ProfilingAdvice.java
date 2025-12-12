package org.sunyaxing.imagine.jdvagent.advices;

import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 方法性能分析和调用链追踪的Advice类
 * 使用Byte Buddy拦截方法调用，记录方法堆栈和调用树形结构
 */
public class ProfilingAdvice {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfilingAdvice.class);

    /**
     * 线程本地变量：存储每个线程的方法调用栈
     */
    public static final ThreadLocal<Deque<CallNode>> CALL_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 线程本地变量：存储每个线程的根节点列表（顶层方法调用）
     */
    public static final ThreadLocal<List<CallNode>> ROOT_NODES = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 全局调用ID生成器，为每次方法调用分配唯一ID
     */
    public static final AtomicLong CALL_ID_GENERATOR = new AtomicLong(0);

    /**
     * 方法进入时的拦截逻辑
     * 创建调用节点，构建调用树，并压入调用栈
     */
    @Advice.OnMethodEnter
    public static CallNode enter(
            @Advice.This Object obj,
            @Advice.Origin Method method,
            @Advice.AllArguments Object[] args) {
        // 获取当前线程的调用栈
        Deque<CallNode> stack = CALL_STACK.get();
        // 创建当前方法调用的节点
        CallNode currentNode = new CallNode(
                CALL_ID_GENERATOR.incrementAndGet(),
                obj.getClass().getName(),
                method.getName(),
                method.getParameterTypes(),
                args,
                System.currentTimeMillis()
        );

        // 如果栈为空，说明是根节点（顶层调用）
        if (stack.isEmpty()) {
            ROOT_NODES.get().add(currentNode);
        } else {
            // 否则，将当前节点作为栈顶节点的子节点
            CallNode parent = stack.peek();
            parent.addChild(currentNode);
            currentNode.setParent(parent);
        }

        // 将当前节点压入调用栈
        stack.push(currentNode);

        // 返回开始时间，用于计算方法执行耗时
        return currentNode;
    }

    /**
     * 方法退出时的拦截逻辑
     * 记录方法执行结果、耗时，并在根节点返回时输出调用树
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.This Object obj,
            @Advice.Enter CallNode currentNode,
            @Advice.Origin Method method,
            @Advice.Return Object ret,
            @Advice.Thrown Throwable throwable,
            @Advice.AllArguments Object[] args) {
        long current = System.currentTimeMillis();
        long cost = current - currentNode.getStartTime();
        // 获取当前线程的调用栈
        Deque<CallNode> stack = CALL_STACK.get();
        if (!stack.isEmpty()) {
            // 弹出当前节点
            stack.pop();
            // 记录结束时间和执行耗时
            currentNode.setEndTime(current);
            currentNode.setDuration(cost);
            // 记录返回值
            currentNode.setReturnValue(ret);
            // 记录异常（如果有）
            currentNode.setException(throwable);

            // 如果栈已空，说明回到了根节点，输出完整的调用树
            if (stack.isEmpty()) {
                LOGGER.info("【JDataViewAgent】 Call Tree:\n{}", currentNode.toTreeString(0));
            }
        }
    }

    /**
     * 获取当前线程的所有根节点
     *
     * @return 根节点列表的副本
     */
    public static List<CallNode> getRootNodes() {
        return new ArrayList<>(ROOT_NODES.get());
    }

    /**
     * 清除当前线程的调用树和调用栈
     * 用于释放内存或重置状态
     */
    public static void clearCallTree() {
        CALL_STACK.get().clear();
        ROOT_NODES.get().clear();
    }

    /**
     * 获取当前方法调用栈的字符串表示
     *
     * @return 调用栈的格式化字符串
     */
    public static String getCurrentStackTrace() {
        Deque<CallNode> stack = CALL_STACK.get();
        if (stack.isEmpty()) {
            return "Empty stack";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Current Call Stack (depth: ").append(stack.size()).append("):\n");
        int depth = 0;
        // 遍历调用栈，输出每一层的方法签名
        for (CallNode node : stack) {
            sb.append("  ".repeat(depth++))
                    .append("→ ")
                    .append(node.getMethodSignature())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 调用节点类
     * 表示方法调用树中的一个节点，记录方法调用的完整信息
     */
    public static class CallNode {
        /**
         * 调用唯一ID
         */
        private final long callId;
        /**
         * 类名
         */
        private final String className;
        /**
         * 方法名
         */
        private final String methodName;
        /**
         * 参数类型数组
         */
        private final Class<?>[] parameterTypes;
        /**
         * 实际参数值数组
         */
        private final Object[] arguments;
        /**
         * 方法开始执行时间
         */
        private final long startTime;
        /**
         * 方法结束执行时间
         */
        private long endTime;
        /**
         * 方法执行耗时（毫秒）
         */
        private long duration;
        /**
         * 方法返回值
         */
        private Object returnValue;
        /**
         * 方法抛出的异常（如果有）
         */
        private Throwable exception;
        /**
         * 父节点（调用当前方法的方法）
         */
        private CallNode parent;
        /**
         * 子节点列表（当前方法调用的其他方法）
         */
        private final List<CallNode> children;

        public CallNode(long callId, String className, String methodName,
                        Class<?>[] parameterTypes, Object[] arguments, long startTime) {
            this.callId = callId;
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.arguments = arguments;
            this.startTime = startTime;
            this.children = new ArrayList<>();
        }

        /**
         * 添加子节点
         */
        public void addChild(CallNode child) {
            children.add(child);
        }

        /**
         * 设置父节点
         */
        public void setParent(CallNode parent) {
            this.parent = parent;
        }

        /**
         * 设置结束时间
         */
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        /**
         * 设置执行耗时
         */
        public void setDuration(long duration) {
            this.duration = duration;
        }

        /**
         * 设置返回值
         */
        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }

        /**
         * 设置异常
         */
        public void setException(Throwable exception) {
            this.exception = exception;
        }

        /**
         * 获取方法签名字符串
         *
         * @return 格式：类名.方法名(参数类型列表)
         */
        public String getMethodSignature() {
            return className + "." + methodName + "(" + Arrays.toString(parameterTypes) + ")";
        }

        /**
         * 将调用树转换为格式化的字符串
         *
         * @param depth 当前节点的深度（用于缩进）
         * @return 树形结构的字符串表示
         */
        public String toTreeString(int depth) {
            StringBuilder sb = new StringBuilder();
            String indent = "  ".repeat(depth);

            // 添加缩进
            sb.append(indent);
            if (depth > 0) {
                sb.append("├─ ");
            }

            // 输出调用ID和方法信息
            sb.append("[").append(callId).append("] ")
                    .append(className).append(".")
                    .append(methodName).append("(");

            // 输出参数
            if (parameterTypes != null && parameterTypes.length > 0) {
                for (Class<?> type : parameterTypes) {
                    sb.append(type.getSimpleName()).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
            }

            sb.append(") ");

            // 输出异常或返回值
            if (exception != null) {
                sb.append("❌ Exception: ").append(exception.getClass().getSimpleName());
            } else if (returnValue != null) {
                sb.append("→ ").append(returnValue);
            }

            // 输出执行耗时
            sb.append(" [").append(duration).append("ms]");
            sb.append("\n");

            // 递归输出所有子节点
            for (int i = 0; i < children.size(); i++) {
                CallNode child = children.get(i);
                sb.append(child.toTreeString(depth + 1));
            }

            return sb.toString();
        }

        public long getCallId() {
            return callId;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public long getDuration() {
            return duration;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public Throwable getException() {
            return exception;
        }

        public CallNode getParent() {
            return parent;
        }

        public List<CallNode> getChildren() {
            return new ArrayList<>(children);
        }
    }
}
