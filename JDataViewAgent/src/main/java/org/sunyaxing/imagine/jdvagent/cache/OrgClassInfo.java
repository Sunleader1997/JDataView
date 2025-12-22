package org.sunyaxing.imagine.jdvagent.cache;

import lombok.Data;

@Data
public class OrgClassInfo {
    private String className;
    private byte[] bytes;
    private ClassLoader classLoader;
}
