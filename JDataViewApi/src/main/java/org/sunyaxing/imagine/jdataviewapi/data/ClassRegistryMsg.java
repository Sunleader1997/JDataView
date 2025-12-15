package org.sunyaxing.imagine.jdataviewapi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 类信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRegistryMsg {
    private String className;
    private List<String> methodNames;
}
