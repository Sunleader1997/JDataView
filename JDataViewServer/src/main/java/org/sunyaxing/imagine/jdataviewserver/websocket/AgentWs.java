package org.sunyaxing.imagine.jdataviewserver.websocket;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;
import org.sunyaxing.imagine.jmemqueue.JSharedMemQueue;
import org.sunyaxing.imagine.jmemqueue.JSharedMemReader;

import java.nio.charset.StandardCharsets;

/**
 * 连线数据传输实时效果
 */
@Slf4j
public class AgentWs {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentWs.class);
    public static final JSharedMemQueue jSharedMemQueue = new JSharedMemQueue("JDataView", 2048);

    public AgentMsgService agentMsgService;

    public AgentWs(AgentMsgService agentMsgService) {
        this.agentMsgService = agentMsgService;
    }

    public void onMessage(String payload) {
        LOGGER.info("收到消息: {}", payload);
        JDataViewMsg jDataViewMsg = JSONObject.parseObject(payload, JDataViewMsg.class);
        // 记录方法调用
        agentMsgService.save(AgentMsgService.parseMsg(jDataViewMsg));
    }

    public void run() {
        new Thread(() -> {
            JSharedMemReader reader = jSharedMemQueue.createReader();
            while (true) {
                byte[] bytes = reader.dequeue();
                if (bytes != null) {
                    String message = new String(bytes, StandardCharsets.UTF_8);
                    onMessage(message);
                } else {
                    ThreadUtil.sleep(10);
                }
            }
        }).start();
    }
}
