package org.sunyaxing.imagine.jdataviewapi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // 业务数据
    private List<String> content;
}
