package org.sunyaxing.imagine.jdvagent.sender;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdvagent.JDataViewAgent;
import org.sunyaxing.imagine.jdvagent.sender.base.Sender;
import org.sunyaxing.imagine.jmemqueue.JSharedMemQueue;

import java.nio.charset.StandardCharsets;

/**
 * 发送器
 * 数据优先送往队列
 * 线程将批量将数据推送至目标
 */
public class JDataViewEventSender implements Sender {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewEventSender.class);

    public static final long PID = ProcessHandle.current().pid();
    public final JSharedMemQueue jSharedMemQueue;

    public JDataViewEventSender() {
        this.jSharedMemQueue = new JSharedMemQueue("JDataView", 2048);
        this.jSharedMemQueue.createWriteCarriage();
    }

    @Override
    public void send(ThreadSpace message) {
        // 发送服务信息
        JDataViewMsg appMsg = JDataViewMsg.builder()
                .appName(JDataViewAgent.configProperties.getAppName())
                .pid(PID)
                .msgType(JDataViewMsg.MsgType.MethodCall)
                .content(message)
                .build();
        String data = JSON.toJSONString(appMsg);
        jSharedMemQueue.enqueue(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() {

    }
}
