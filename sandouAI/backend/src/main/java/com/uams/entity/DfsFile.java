package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dfs_file")
public class DfsFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String extension;

    private String mimeType;

    private Long size;

    private String md5;

    private String storagePath;

    private String bucket;

    private Integer status;

    private Long ownerId;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
