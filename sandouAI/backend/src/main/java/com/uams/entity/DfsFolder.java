package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dfs_folder")
public class DfsFolder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long parentId;

    private Long ownerId;

    private String type;

    private Long teamId;

    private Integer sortOrder;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
