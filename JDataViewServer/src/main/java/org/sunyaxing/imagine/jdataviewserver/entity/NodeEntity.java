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

    @TableField("jobId")
    private String jobId;

    @TableField("name")
    private String name;

    @TableField("nodeType")
    private String nodeType;

    @TableField("pluginId")
    private String pluginId;

    @TableField("config")
    private String config;

    @TableField("X")
    private Integer x;

    @TableField("Y")
    private Integer y;

    @TableField(value = "width", updateStrategy = FieldStrategy.ALWAYS)
    private Integer width;

    @TableField(value = "height", updateStrategy = FieldStrategy.ALWAYS)
    private Integer height;

    @TableField(value = "parentNode", updateStrategy = FieldStrategy.ALWAYS)
    private String parentNode;

    @TableField(value = "extent", updateStrategy = FieldStrategy.ALWAYS)
    private String extent;

    @TableField("handles")
    private String handles;

    @TableField("tags")
    private String tags;
}
