package org.sunyaxing.imagine.jdataviewserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * transflow 各节点
 */

@Builder
@Data
@ToString
@TableName("node")
public class NodeEntity implements Serializable {
    @TableField("id")
    private String id;

    @TableField("name")
    private String name;

    @TableField("nodeType")
    private String nodeType;

    @TableField("X")
    private Integer x;

    @TableField("Y")
    private Integer y;

    @TableField(value = "width", updateStrategy = FieldStrategy.ALWAYS)
    private Integer width;

    @TableField(value = "height", updateStrategy = FieldStrategy.ALWAYS)
    private Integer height;

    @TableField("handles")
    private String handles;
}
