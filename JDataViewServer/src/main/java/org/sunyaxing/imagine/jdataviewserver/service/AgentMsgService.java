package org.sunyaxing.imagine.jdataviewserver.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.LifeCycle;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.cover.EntityCover;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AgentMsgRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Map<Long, MethodCall> generateBy(String appName) {
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
     *
     * @param messages 属于同一个线程的消息列表，已按ID排序
     * @return 构建好的方法调用堆栈列表
     */
    private MethodCall buildCallStack(List<AgentMsgEntity> messages) {
        MethodCall root = new MethodCall();
        // 找到根节点 depth 0 && state ENTER
        // 正常来说第一个数据就是根节点，如果不是说明数据有问题
        return root;
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
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodCall {
        private String className;
        private String methodName;
        private long startTime;
        private long endTime = -1; // 默认值表示未结束
        private List<MethodCall> children = new ArrayList<>();
    }
}