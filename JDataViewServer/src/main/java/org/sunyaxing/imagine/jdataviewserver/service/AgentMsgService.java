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
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.ThreadDto;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.cover.EntityCover;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AgentMsgRepository;

import java.util.*;

@Service
public class AgentMsgService extends ServiceImpl<AgentMsgRepository, AgentMsgEntity> {
    public static final String PREFIX = "MSG-";

    public List<JavaAppDto> generateJavaAppDto() {
        List<AgentMsgEntity> appDataList = lambdaQuery()
                .select(AgentMsgEntity::getAppName)
                .groupBy(AgentMsgEntity::getAppName).list();
        return appDataList.stream().map(agentMsgEntity -> {
            return JavaAppDto.builder().host("127.0.0.1").appName(agentMsgEntity.getAppName()).pid(0L).build();
        }).toList();
    }

    public List<ThreadDto> generateThreadDto(JavaAppDto javaAppDto) {
        List<AgentMsgEntity> threadList = lambdaQuery()
                .select(AgentMsgEntity::getThreadId, AgentMsgEntity::getThreadName)
                .eq(AgentMsgEntity::getAppName, javaAppDto.getAppName())
                //.eq(AgentMsgEntity::getPid, javaAppDto.getPid())
                .groupBy(AgentMsgEntity::getThreadId, AgentMsgEntity::getThreadName)
                .list();
        return threadList.stream().map(agentMsgEntity -> {
            return ThreadDto.builder().threadId(agentMsgEntity.getThreadId()).threadName(agentMsgEntity.getThreadName()).build();
        }).toList();
    }

    public static AgentMsgEntity parseMsg(JDataViewMsg agentMsg) {
        ThreadSpace threadSpace = agentMsg.getContent();
        AgentMsgEntity entity = EntityCover.INSTANCE.msgToEntity(threadSpace);
        entity.setId(PREFIX + IdUtil.getSnowflakeNextIdStr());
        entity.setAppName(agentMsg.getAppName());
        entity.setPid(agentMsg.getPid());
        return entity;
    }

    /**
     * 获取 APP 所有的链路
     */
    public List<MethodCall> generateBy(String appName, String threadId) {
        // 获取日志, 根据ID进行排序可以保证方法执行的先后顺序
        List<AgentMsgEntity> agentMsgEntities = this.lambdaQuery()
                .eq(AgentMsgEntity::getAppName, appName)
                .eq(AgentMsgEntity::getThreadId, threadId)
                .orderByAsc(AgentMsgEntity::getId)
                .list();
        // 为每个线程构建调用堆栈
        List<MethodCall> methodCall = buildCallStack(agentMsgEntities); // 调用辅助方法
        Collections.reverse(methodCall);
        return methodCall; // 返回所有线程的堆栈集合
    }
    /**
     * 清空应用记录
     */
    public void clearBy(String appName) {
        this.lambdaUpdate()
                .eq(AgentMsgEntity::getAppName, appName)
                .remove();
    }

    /**
     * 为单个线程的消息列表构建调用堆栈
     * TODO 如何将堆栈深度控制在一定范围内？
     *
     * @param messages 属于同一个线程的消息列表，已按ID排序
     * @return 构建好的方法调用堆栈列表
     */
    private List<MethodCall> buildCallStack(List<AgentMsgEntity> messages) {
        List<MethodCall> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(messages)) return null;
        // 用栈来维护当前的调用链
        Deque<MethodCall> callStack = new ArrayDeque<>();
        // 正常来说第一个数据就是根节点，如果不是说明数据有问题
        for (AgentMsgEntity next : messages) {
            switch (next.getMethodState()) {
                case ENTER -> {
                    // 如果堆栈排空，则创建根节点
                    if (callStack.isEmpty()) {
                        MethodCall rootMethod = MethodCall.buildRoot(next);
                        // 将根节点加入结果
                        result.add(rootMethod);
                        callStack.push(rootMethod);
                    } else {
                        MethodCall childStartMethod = MethodCall.buildRoot(next);
                        // 获取栈顶元素（不移除）
                        MethodCall parent = callStack.peek();
                        if (parent != null) {
                            parent.addChild(childStartMethod);
                        }
                        callStack.push(childStartMethod);
                    }
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
        return result;
    }

    // 内部类：表示一次方法调用及其子调用
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MethodCall {
        private String id;
        private String className;
        private String methodName;
        private long startTime;
        private long endTime = -1; // 默认值表示未结束
        private long cost = -1;
        private long depth;
        private List<MethodCall> children = new ArrayList<>();

        public static MethodCall buildRoot(AgentMsgEntity agentMsgEntity) {
            if (!agentMsgEntity.isMethodStart()) throw new RuntimeException("当前节点非ENTER节点");
            return MethodCall.builder()
                    .id(agentMsgEntity.getId())
                    .className(agentMsgEntity.getClassName())
                    .methodName(agentMsgEntity.getMethodName())
                    .startTime(agentMsgEntity.getMethodStartTime())
                    .depth(agentMsgEntity.getDepth())
                    .children(new ArrayList<>())
                    .build();
        }


        public void addChild(MethodCall nextRoot) {
            this.children.add(nextRoot);
        }

        @Override
        public String toString() {
            return buildTreeString(0, true, true);
        }

        /**
         * 构建树形结构字符串
         * @param level 当前层级
         * @param isLast 当前节点是否是同级最后一个节点
         * @param isRoot 是否是根节点
         * @return 树形结构字符串
         */
        private String buildTreeString(int level, boolean isLast, boolean isRoot) {
            StringBuilder sb = new StringBuilder();

            // 构建当前节点的前缀
            if (!isRoot) {
                // 添加缩进
                for (int i = 0; i < level - 1; i++) {
                    sb.append("│   ");
                }
                // 添加当前级别的连接符
                if (level > 0) {
                    sb.append(isLast ? "└── " : "├── ");
                }
            }

            // 添加当前节点信息
            sb.append(className).append(" ").append(methodName).append(" ");
            if (cost >= 0) {
                sb.append(cost).append(" ms");
            } else {
                sb.append("(not finished)");
            }
            sb.append("\n");

            // 递归处理子节点
            for (int i = 0; i < children.size(); i++) {
                MethodCall child = children.get(i);
                boolean isLastChild = (i == children.size() - 1);
                sb.append(child.buildTreeString(level + 1, isLastChild, false));
            }

            return sb.toString();
        }

    }
}