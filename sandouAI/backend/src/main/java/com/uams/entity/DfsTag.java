package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dfs_tag")
public class DfsTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String color;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
