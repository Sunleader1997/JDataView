package org.sunyaxing.imagine.jdvagent.cache;

import net.bytebuddy.dynamic.DynamicType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrgClassByteManager {
    private static final Map<String, OrgClassInfo> CLASS_BYTE_MAP = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(OrgClassByteManager.class);
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    public static void put(String className, ClassLoader classLoader, DynamicType.Builder<?> builder) {
        try {
            OrgClassInfo orgClassInfo = new OrgClassInfo();
            orgClassInfo.setClassName(className);
            orgClassInfo.setBytes(builder.make().getBytes());
            orgClassInfo.setClassLoader(classLoader);
            CLASS_BYTE_MAP.put(className, orgClassInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void buildToTemp(DynamicType.Builder<?> builder) {
        try {
            builder.make().saveIn(tmpDir).forEach((key, value) -> {
                log.info("INSTALL {} VIEW {}", key, value.getAbsolutePath());
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
                log.error(e.getMessage(), e);
            }
        }
    }
}
