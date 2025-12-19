package org.sunyaxing.imagine.jdataviewserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.sunyaxing.imagine.jdataviewapi.data.LifeCycle;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@TableName("AGENT_MSG")
public class AgentMsgEntity implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("APP_NAME")
    private String appName;
    @TableField("PID")
    private Long pid;
    // 线程相关
    @TableField("THREAD_ID")
    private Long threadId;
    @TableField("THREAD_NAME")
    private String threadName;
    @TableField("STEP_INDEX")
    private Long stepIndex;
    // 调用深度
    @TableField("DEPTH")
    private Long depth;
    // 类与方法栈
    @TableField("CLASS_NAME")
    private String className;
    @TableField("METHOD_NAME")
    private String methodName;
    // 方法调用的开始时间
    @TableField("METHOD_START_TIME")
    private Long methodStartTime;
    // 当前方法的状态
    @TableField("METHOD_STATE")
    private LifeCycle.MethodState methodState;
    // 方法调用的结束时间
    @TableField("METHOD_END_TIME")
    private Long methodEndTime;

    // 辅助方法：判断消息是否代表方法开始
    // 这个方法需要根据您的 LifeCycle.MethodState 枚举来实现
    public boolean isMethodStart() {
        return LifeCycle.MethodState.ENTER.equals(getMethodState());
    }

    // 辅助方法：判断消息是否代表方法结束
    // 这个方法需要根据您的 LifeCycle.MethodState 枚举来实现
    public boolean isMethodEnd() {
        return LifeCycle.MethodState.SUC.equals(getMethodState());
    }
    public long generateCost() {
        return getMethodEndTime() - getMethodStartTime();
    }
}
