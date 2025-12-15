package org.sunyaxing.imagine.jdvagent.sender;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.ClassRegistryMsg;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
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
public class JDataViewEventSender implements Sender {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewEventSender.class);

    private final EventQueue eventQueue;
    private final EventQueue registryQueue;
    private final ExecutorService executor;
    public static final long PID = ProcessHandle.current().pid();
    public static final String APP_NAME = System.getProperty("sun.java.command");

    public JDataViewEventSender() {
        this.eventQueue = new EventQueue();
        this.registryQueue = new EventQueue();
        this.executor = Executors.newFixedThreadPool(2);
        executeThreadSpace();
        executeClassRegistryMsg();
    }

    @Override
    public void send(Object message) {
        String data = JSONObject.toJSONString(message);
        if (message instanceof ThreadSpace) {
            this.eventQueue.put(data);
        } else if (message instanceof ClassRegistryMsg) {
            this.registryQueue.put(data);
        } else {
            LOGGER.warn(LogDicts.LOG_PREFIX + "send error, message is not JDataViewMsg or ClassRegistryMsg");
        }
    }

    // 消费event数据
    public void executeThreadSpace() {
        this.executor.submit(() -> {
            while (!Thread.interrupted()) {
                List<String> res = this.eventQueue.pull();
                if (!res.isEmpty()) {
                    // 发送服务信息
                    JDataViewMsg appMsg = JDataViewMsg.builder()
                            .appName(APP_NAME)
                            .pid(PID)
                            .msgType(JDataViewMsg.MsgType.MethodCall)
                            .content(res)
                            .build();
                    JDataViewWebSocketClient.getInstance().send(JSONObject.toJSONString(appMsg));
                }
            }
        });
    }

    // 消费regist数据
    public void executeClassRegistryMsg() {
        this.executor.submit(() -> {
            while (!Thread.interrupted()) {
                List<String> res = this.registryQueue.pull();
                if (!res.isEmpty()) {
                    // 发送服务信息
                    JDataViewMsg appMsg = JDataViewMsg.builder()
                            .appName(APP_NAME)
                            .pid(PID)
                            .msgType(JDataViewMsg.MsgType.ClassRegister)
                            .content(res)
                            .build();
                    JDataViewWebSocketClient.getInstance().send(JSONObject.toJSONString(appMsg));
                }
            }
        });
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
