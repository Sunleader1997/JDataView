package org.sunyaxing.imagine.jdvagent.sender;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdvagent.JDataViewAgent;
import org.sunyaxing.imagine.jdvagent.sender.base.EventQueue;
import org.sunyaxing.imagine.jdvagent.sender.base.JDataViewWebSocketClient;
import org.sunyaxing.imagine.jdvagent.sender.base.Sender;

/**
 * 发送器
 * 数据优先送往队列
 * 线程将批量将数据推送至目标
 */
public class JDataViewEventSender implements Sender {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewEventSender.class);

    private final EventQueue eventQueue;
    public static final long PID = ProcessHandle.current().pid();

    public JDataViewEventSender() {
        this.eventQueue = new EventQueue(res -> {
            // 发送服务信息
            JDataViewMsg appMsg = JDataViewMsg.builder()
                    .appName(JDataViewAgent.configProperties.getAppName())
                    .pid(PID)
                    .msgType(JDataViewMsg.MsgType.MethodCall)
                    .content(res)
                    .build();
            JDataViewWebSocketClient.getInstance().send(JSONObject.toJSONString(appMsg));
        });
    }

    @Override
    public void send(ThreadSpace message) {
        String data = JSON.toJSONString(message);
        this.eventQueue.put(data);
    }

    @Override
    public void close() {
        this.eventQueue.destroy();
    }
}
