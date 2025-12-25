package org.sunyaxing.imagine.jdvagent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrgClassByteManager {
    private static final Map<String, OrgClassInfo> CLASS_BYTE_MAP = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(OrgClassByteManager.class);

    public static void put(String className, ClassLoader classLoader, byte[] bytes) {
        try {
            OrgClassInfo orgClassInfo = new OrgClassInfo();
            orgClassInfo.setClassName(className);
            orgClassInfo.setBytes(bytes);
            orgClassInfo.setClassLoader(classLoader);
            CLASS_BYTE_MAP.put(className, orgClassInfo);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    public static OrgClassInfo get(String className) {
        return CLASS_BYTE_MAP.get(className);
    }

    public static void restore(String className, Instrumentation instrumentation) throws Exception {
        OrgClassInfo orgClassInfo = CLASS_BYTE_MAP.get(className);
        if (orgClassInfo != null) {
            Class<?> targetClass = orgClassInfo.getClassLoader().loadClass(className);
            ClassDefinition definition = new ClassDefinition(targetClass, orgClassInfo.getBytes());
            // 重新定义类为原始字节码
            instrumentation.redefineClasses(definition);
        }
    }

    public static void restore(Instrumentation instrumentation) {
        for (String className : CLASS_BYTE_MAP.keySet()) {
            try {
                restore(className, instrumentation);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }
}
