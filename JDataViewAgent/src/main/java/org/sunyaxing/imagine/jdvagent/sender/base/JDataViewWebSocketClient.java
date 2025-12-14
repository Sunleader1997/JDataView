package org.sunyaxing.imagine.jdvagent.sender.base;

import com.alibaba.fastjson2.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdataviewapi.data.AppMsg;
import org.sunyaxing.imagine.jdvagent.dicts.LogDicts;

import java.net.URI;

/**
 * 负责将数据推送到 SERVER
 * 1. 连接建立时 发送服务信息
 */
public class JDataViewWebSocketClient extends WebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewWebSocketClient.class);
    public static final String DEFAULT_SERVER_URI = "ws://127.0.0.1:19876/agent";
    public static JDataViewWebSocketClient INSTANCE;
    public static final long PID = ProcessHandle.current().pid();
    public static final String APP_NAME = System.getProperty("sun.java.command");

    public static JDataViewWebSocketClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JDataViewWebSocketClient(URI.create(DEFAULT_SERVER_URI));
            INSTANCE.connect();
        }
        return INSTANCE;
    }

    public JDataViewWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOGGER.info(LogDicts.LOG_PREFIX + "连接已建立");
        // 发送服务信息
        AppMsg appMsg = AppMsg.builder()
                .appName(APP_NAME)
                .pid(PID)
                .build();
        send(JSONObject.toJSONString(appMsg));
        // TODO 心跳与重连
    }

    @Override
    public void onMessage(String s) {
        LOGGER.info(LogDicts.LOG_PREFIX + "收到消息 {}", s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        LOGGER.info(LogDicts.LOG_PREFIX + "连接已关闭");
    }

    @Override
    public void onError(Exception e) {
        LOGGER.error(LogDicts.LOG_PREFIX + "连接异常, {}", INSTANCE.getURI().toString());
    }

    @Override
    public void send(String text) {
        try {
            LOGGER.info(LogDicts.LOG_PREFIX + "发送数据 {}", text);
            super.send(text);
        } catch (WebsocketNotConnectedException unlink) {
            LOGGER.error(LogDicts.LOG_PREFIX + "websocket 未连接 {}", INSTANCE.getURI().toString());
        } catch (Exception e) {
            LOGGER.error(LogDicts.LOG_PREFIX + "发送数据异常", e);
        }
    }
}
