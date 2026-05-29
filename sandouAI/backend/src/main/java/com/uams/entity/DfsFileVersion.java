package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dfs_file_version")
public class DfsFileVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fileId;

    private Integer versionNum;

    private String storagePath;

    private Long size;

    private String md5;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
