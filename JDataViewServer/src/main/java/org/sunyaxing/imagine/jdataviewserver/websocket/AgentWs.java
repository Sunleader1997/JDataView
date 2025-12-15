package org.sunyaxing.imagine.jdataviewserver.websocket;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;
import org.sunyaxing.imagine.jdataviewserver.service.AppService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 连线数据传输实时效果
 */
@Slf4j
@Component
@ServerEndpoint(value = "/agent")
public class AgentWs {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentWs.class);
    // 保存所有连接的会话
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    public AgentMsgService agentMsgService;
    public AppService appService;

    public AgentWs() {
        this.agentMsgService = SpringUtil.getBean(AgentMsgService.class);
        this.appService = SpringUtil.getBean(AppService.class);
    }

    @OnOpen
    public void onOpen(Session session) {
        SESSION_MAP.put(session.getId(), session);
        LOGGER.info("WebSocket 连接建立: {}", session.getId());
        try {
            session.getBasicRemote().sendText("连接成功！");
        } catch (Exception e) {
            LOGGER.error("发送消息失败：{}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("app") String app) {
        SESSION_MAP.remove(session.getId());
        LOGGER.info("WebSocket 连接关闭: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String payload, Session session) {
        LOGGER.info("收到消息: {}", payload);
        JDataViewMsg<ThreadSpace> jDataViewMsg = JSONObject.parseObject(payload, new TypeReference<>() {
        });
        // 根据 agentMsg 创建应用
        appService.insertByAgentMsg(jDataViewMsg);
        // 存储消息
        agentMsgService.saveBatch(AgentMsgService.parseMsg(jDataViewMsg));
    }
}
