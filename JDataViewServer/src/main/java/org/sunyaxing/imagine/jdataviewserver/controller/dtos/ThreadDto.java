package org.sunyaxing.imagine.jdataviewserver.controller.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDto {
    private Long threadId;
    private String threadName;
}
