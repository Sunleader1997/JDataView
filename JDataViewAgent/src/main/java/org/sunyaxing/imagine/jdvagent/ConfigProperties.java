package org.sunyaxing.imagine.jdvagent;

import lombok.Data;

@Data
public class ConfigProperties {
    private static final String APP_COMMAND = System.getProperty("sun.java.command");

    private boolean attached;
    private String mode;
    private String endpoint = "ws://127.0.0.1:19876/agent";
    private String scanPack = "";
    private String appName = "";

    public void valid() {
        if (mode.isEmpty()) {
            throw new RuntimeException("mode is empty");
        }
    }

    public String getAppName() {
        if (appName.isEmpty()) {
            return APP_COMMAND;
        }
        return appName;
    }
}
