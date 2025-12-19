package org.sunyaxing.imagine.jdvagent;

import lombok.Data;

@Data
public class ConfigProperties {
    private String endpoint = "ws://127.0.0.1:19876/agent";
    private String scanPack = "";

    public void valid() {
        if (scanPack.isEmpty()) {
            throw new RuntimeException("scanPack is empty");
        }
        if (endpoint.isEmpty()) {
            throw new RuntimeException("endpoint is empty");
        }
    }
}
