package org.sunyaxing.imagine.jdataviewserver.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.ClassRegistryMsg;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.LifeCycle;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.NodeEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.cover.EntityCover;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AgentMsgRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentMsgService extends ServiceImpl<AgentMsgRepository, AgentMsgEntity> {
    public static final String PREFIX = "MSG-";

    public static List<AgentMsgEntity> parseMsg(JDataViewMsg agentMsg) {
        return agentMsg.getContent().stream().map(strData -> {
            ThreadSpace threadSpace = JSONObject.parseObject(strData, ThreadSpace.class);
            AgentMsgEntity entity = EntityCover.INSTANCE.msgToEntity(threadSpace);
            entity.setId(PREFIX + IdUtil.getSnowflakeNextIdStr());
            entity.setAppName(agentMsg.getAppName());
            entity.setPid(agentMsg.getPid());
            return entity;
        }).toList();
    }

    /**
     * 获取 APP 所有的链路
     */
    public Map<Long, List<MethodCall>> generateBy(String appName) {
        // 获取日志, 根据ID进行排序可以保证方法执行的先后顺序
        List<AgentMsgEntity> agentMsgEntities = this.lambdaQuery()
                .eq(AgentMsgEntity::getAppName, appName)
                .orderByAsc(AgentMsgEntity::getId)
                .list();
        // 将日志转换成堆栈
        // 2. 按线程ID分组
        Map<Long, List<AgentMsgEntity>> groupedByThread = agentMsgEntities.stream()
                .collect(Collectors.groupingBy(AgentMsgEntity::getThreadId));

        // 3. 为每个线程构建调用堆栈
        Map<Long, List<MethodCall>> threadCallStacks = new HashMap<>();
        for (Map.Entry<Long, List<AgentMsgEntity>> entry : groupedByThread.entrySet()) {
            Long threadId = entry.getKey();
            List<AgentMsgEntity> messagesForThread = entry.getValue();
            List<MethodCall> callStack = buildCallStack(messagesForThread); // 调用辅助方法
            threadCallStacks.put(threadId, callStack);
        }

        return threadCallStacks; // 返回所有线程的堆栈集合
    }

    /**
     * 为单个线程的消息列表构建调用堆栈
     * @param messagesForThread 属于同一个线程的消息列表，已按ID排序
     * @return 构建好的方法调用堆栈列表
     */
    private List<MethodCall> buildCallStack(List<AgentMsgEntity> messagesForThread) {
        if (messagesForThread == null || messagesForThread.isEmpty()) {
            return Collections.emptyList();
        }

        Deque<MethodCall> stack = new LinkedList<>(); // 使用双端队列模拟调用栈
        List<MethodCall> result = new ArrayList<>();   // 存储最终的树形结构根节点

        for (AgentMsgEntity msg : messagesForThread) {
            String methodName = msg.getMethodName();
            String className = msg.getClassName();
            long startTime = msg.getMethodStartTime();
            long endTime = msg.getMethodEndTime();
            // 假设 MethodState 是枚举类型，包含 START 和 END
            // LifeCycle.MethodState state = msg.getMethodState();

            // 这里需要根据您的 JDataViewMsg 或 AgentMsgEntity 中的 LifeCycle.MethodState 来判断是进入还是退出
            // 通常，一个完整的调用会有两个消息：一个标记开始(ENTER/START)，一个标记结束(EXIT/END)
            // 示例假设：
            // - 如果 state 是 ENTER，则压入栈
            // - 如果 state 是 EXIT，则弹出栈，并设置结束时间

            // --- 伪代码逻辑 ---
            if (isMethodStart(msg)) { // 需要实现这个判断逻辑
                MethodCall call = new MethodCall(className, methodName, startTime);
                if (!stack.isEmpty()) {
                    // 如果栈不为空，当前调用是栈顶元素的子调用
                    stack.peekLast().addChild(call);
                } else {
                    // 如果栈为空，说明这是一个顶层调用
                    result.add(call);
                }
                stack.offerLast(call); // 入栈
            } else if (isMethodEnd(msg)) { // 需要实现这个判断逻辑
                if (!stack.isEmpty() && stack.peekLast().matches(className, methodName)) {
                    MethodCall completedCall = stack.pollLast(); // 出栈
                    completedCall.setEndTime(endTime); // 设置结束时间
                    // 可以在这里添加其他后处理逻辑
                } else {
                    // 错误处理：END 消息没有匹配的 START 消息，或者顺序错误
                    // 可能是数据问题，可以选择忽略或记录警告
                    System.err.println("Unmatched END message for: " + className + "." + methodName +
                            " on thread " + msg.getThreadId() + " at " + endTime);
                }
            }
            // --- 伪代码逻辑结束 ---

            // --- 如果消息包含完整信息的替代方案 ---
            // 如果一条消息本身就包含了完整的调用信息（既有开始又有结束），则可以直接创建MethodCall并添加到结果中
            // if (msg.getMethodState() == LifeCycle.MethodState.COMPLETE) { // 假设有个COMPLETE状态
            //     MethodCall call = new MethodCall(className, methodName, startTime, endTime);
            //     if (!stack.isEmpty()) {
            //         stack.peekLast().addChild(call);
            //     } else {
            //         result.add(call);
            //     }
            //     // 注意：如果是一条消息代表完成，这里可能不需要入栈或出栈，除非还要处理嵌套
            //     // 如果是完整的一次调用且无内部嵌套，可能不需要维护栈
            // }
        }

        // 检查是否有未结束的方法调用
        if (!stack.isEmpty()) {
            System.err.println("Unmatched START messages remaining in stack after processing thread.");
            // 可以选择将这些未完成的调用也加入结果，并标记为异常或设置默认结束时间
            while (!stack.isEmpty()) {
                MethodCall incompleteCall = stack.pollFirst();
                incompleteCall.setEndTime(System.currentTimeMillis()); // 或者设置为一个特殊值表示未正常结束
                incompleteCall.markAsIncomplete(); // 可选：添加一个标记
                // 添加到结果中的合适位置取决于您的需求
                // 如果栈底是顶层调用，可以尝试将其添加到result（但逻辑会变得复杂）
                // 通常这种情况表示数据收集或解析有问题
            }
        }

        return result;
    }

    // 辅助方法：判断消息是否代表方法开始
    // 这个方法需要根据您的 LifeCycle.MethodState 枚举来实现
    private boolean isMethodStart(AgentMsgEntity msg) {
        return LifeCycle.MethodState.ENTER.equals(msg.getMethodState());
    }

    // 辅助方法：判断消息是否代表方法结束
    // 这个方法需要根据您的 LifeCycle.MethodState 枚举来实现
    private boolean isMethodEnd(AgentMsgEntity msg) {
        return LifeCycle.MethodState.SUC.equals(msg.getMethodState());
    }

    // 内部类：表示一次方法调用及其子调用
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MethodCall {
        private String className;
        private String methodName;
        private long startTime;
        private long endTime = -1; // 默认值表示未结束
        private List<MethodCall> children = new ArrayList<>();
        private boolean incomplete = false; // 标记是否因数据问题未能正常结束

        public MethodCall(String className, String methodName, long startTime) {
            this.className = className;
            this.methodName = methodName;
            this.startTime = startTime;
        }

        public void addChild(MethodCall child) {
            this.children.add(child);
        }

        public boolean matches(String otherClass, String otherMethod) {
            // 可以根据需要决定匹配规则，比如只匹配方法名，或类名+方法名
            return this.className.equals(otherClass) && this.methodName.equals(otherMethod);
        }

        public void markAsIncomplete() {
            this.incomplete = true;
        }

        // 计算持续时间的便捷方法
        public long getDuration() {
            return endTime != -1 ? endTime - startTime : -1;
        }
    }
}