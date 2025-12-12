package org.sunyaxing.imagine.jdvagent.sender;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunyaxing.imagine.jdvagent.dicts.LogDicts;

import java.net.URI;

public class JDataViewWebSocketClient extends WebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDataViewWebSocketClient.class);
    public static final String DEFAULT_SERVER_URI = "ws://127.0.0.1:19876/ws";
    public static JDataViewWebSocketClient INSTANCE;

    public static JDataViewWebSocketClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JDataViewWebSocketClient(URI.create(DEFAULT_SERVER_URI));
        }
        INSTANCE.connect();
        return INSTANCE;
    }

    public JDataViewWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOGGER.info(LogDicts.LOG_PREFIX + "连接已建立");
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
        LOGGER.error(LogDicts.LOG_PREFIX + "连接异常", e);
    }

    @Override
    public void send(String text) {
        try {
            LOGGER.info(LogDicts.LOG_PREFIX + "发送数据 {}", text);
            super.send(text);
        } catch (Exception e) {
            LOGGER.error(LogDicts.LOG_PREFIX + "发送数据异常", e);
        }
    }
}
