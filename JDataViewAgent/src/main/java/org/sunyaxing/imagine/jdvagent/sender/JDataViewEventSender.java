package org.sunyaxing.imagine.jdvagent.sender;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdvagent.dicts.LogDicts;
import org.sunyaxing.imagine.jdvagent.sender.base.EventQueue;
import org.sunyaxing.imagine.jdvagent.sender.base.JDataViewWebSocketClient;
import org.sunyaxing.imagine.jdvagent.sender.base.Sender;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发送器
 * 数据优先送往队列
 * 线程将批量将数据推送至目标
 */
public class JDataViewEventSender implements Sender, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewEventSender.class);

    private final EventQueue eventQueue;
    private final ExecutorService executor;
    public static final long PID = ProcessHandle.current().pid();
    public static final String APP_NAME = System.getProperty("sun.java.command");

    public JDataViewEventSender() {
        this.eventQueue = new EventQueue();
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.submit(this);
    }

    @Override
    public void send(Object message) {
        this.eventQueue.put(message);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            List<Object> res = this.eventQueue.pull();
            // 发送服务信息
            JDataViewMsg appMsg = JDataViewMsg.builder()
                    .appName(APP_NAME)
                    .pid(PID)
                    .content(res)
                    .build();
            JDataViewWebSocketClient.getInstance().send(JSONObject.toJSONString(appMsg));
        }
    }

    @Override
    public void close() {
        try {
            this.executor.shutdownNow();
        } catch (Exception e) {
            LOGGER.warn(LogDicts.LOG_PREFIX + "close error", e);
        }
    }
}
