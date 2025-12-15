package org.sunyaxing.imagine.jdataviewserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.sunyaxing.imagine.jdataviewapi.data.LifeCycle;

import java.util.concurrent.atomic.AtomicLong;

@Builder
@Data
@ToString
@TableName("AGENT_MSG")
public class AgentMsgEntity {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private String appName;
    private long pid;
    // 线程相关
    private long threadId;
    private String threadName;
    private Long stepIndex;
    // 类与方法栈
    private String className;
    private String methodName;
    // 方法调用的开始时间
    private long methodStartTime;
    // 当前方法的状态
    private LifeCycle.MethodState methodState;
    // 方法调用的结束时间
    private long methodEndTime;
}
