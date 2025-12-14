package org.sunyaxing.imagine.jdataviewapi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础应用信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMsg {
    // 应用名称
    private String appName;
    private long pid;
}
