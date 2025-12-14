package org.sunyaxing.imagine.jdataviewserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
@TableName("APP")
public class AppEntity {
    @TableField("id")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("name")
    private String name;
    @TableField("description")
    private String description;
}
