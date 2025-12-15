package org.sunyaxing.imagine.jdataviewapi.data;

import lombok.*;

import java.util.List;

/**
 * AGENT 基础信息包裹需要传输的业务数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JDataViewMsg {
    // 应用名称
    private String appName;
    private long pid;
    private MsgType msgType;
    // 业务数据
    private List<String> content;

    @Getter
    @AllArgsConstructor
    public enum MsgType {
        MethodCall("方法执行"),
        ClassRegister("类注册"),
        ;
        private final String description;
    }
}
