package org.sunyaxing.imagine.jdataviewserver.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
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
     *
     * @param rootId TODO 将此节点作为根节点
     */
    public Map<Long, MethodCall> generateBy(String rootId, String appName) {
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
        Map<Long, MethodCall> threadCallStacks = new HashMap<>();
        for (Map.Entry<Long, List<AgentMsgEntity>> entry : groupedByThread.entrySet()) {
            Long threadId = entry.getKey();
            List<AgentMsgEntity> messagesForThread = entry.getValue();
            MethodCall methodCall = buildCallStack(messagesForThread); // 调用辅助方法
            threadCallStacks.put(threadId, methodCall);
        }

        return threadCallStacks; // 返回所有线程的堆栈集合
    }

    /**
     * 为单个线程的消息列表构建调用堆栈
     * TODO 如何将堆栈深度控制在一定范围内？
     *
     * @param messages 属于同一个线程的消息列表，已按ID排序
     * @return 构建好的方法调用堆栈列表
     */
    private MethodCall buildCallStack(List<AgentMsgEntity> messages) {
        if (CollectionUtil.isEmpty(messages)) return null;
        // 用栈来维护当前的调用链
        Deque<MethodCall> callStack = new ArrayDeque<>();
        // 正常来说第一个数据就是根节点，如果不是说明数据有问题
        Iterator<AgentMsgEntity> iterator = messages.iterator();
        AgentMsgEntity firstMessage = iterator.next();
        MethodCall root = MethodCall.buildRoot(firstMessage);
        callStack.push(root);
        while (iterator.hasNext()) {
            // 如果栈已经排空了，说明后面的数据不属于该根节点堆栈
            if (callStack.isEmpty()) {
                return root;
            }
            AgentMsgEntity next = iterator.next();
            switch (next.getMethodState()) {
                case ENTER -> {
                    MethodCall childStartMethod = MethodCall.buildRoot(next);
                    // 获取栈顶元素（不移除）
                    callStack.peek().addChild(childStartMethod);
                    callStack.push(childStartMethod);
                }
                case SUC, EXCEPTION -> {
                    // 栈弹出
                    MethodCall currentCall = callStack.pop();
                    // 设置结束时间
                    currentCall.setEndTime(next.getMethodEndTime());
                    currentCall.setCost(next.generateCost());
                }
            }
        }
        return root;
    }

    // 内部类：表示一次方法调用及其子调用
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodCall {
        private String className;
        private String methodName;
        private long startTime;
        private long endTime = -1; // 默认值表示未结束
        private long cost = -1;
        private List<MethodCall> children = new ArrayList<>();

        public static MethodCall buildRoot(AgentMsgEntity agentMsgEntity) {
            if (!agentMsgEntity.isMethodStart()) throw new RuntimeException("当前节点非ENTER节点");
            return MethodCall.builder()
                    .className(agentMsgEntity.getClassName())
                    .methodName(agentMsgEntity.getMethodName())
                    .startTime(agentMsgEntity.getMethodStartTime())
                    .children(new ArrayList<>())
                    .build();
        }


        public void addChild(MethodCall nextRoot) {
            this.children.add(nextRoot);
        }
    }
}