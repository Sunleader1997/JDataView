package org.sunyaxing.imagine.jdataviewserver.controller.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JavaAppDto {
    private Long pid;
    private String appName;
    private String scanPackage;
    private String host;
    private boolean alive = false;
    private boolean hasLog = false;
    private boolean hasAttached = false;
}
