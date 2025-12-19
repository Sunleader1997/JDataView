package org.sunyaxing.imagine.jdataviewserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Builder
@Data
@ToString
@TableName("APP")
public class AppEntity implements Serializable {
    @TableField("pid")
    private Long pid;
    @TableField("name")
    private String name;
    @TableField("host")
    private String host;
}
