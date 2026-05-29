package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dfs_share")
public class DfsShare {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private Long fileId;

    private Long folderId;

    private String password;

    private LocalDateTime expireTime;

    private Integer allowDownload;

    private Integer visitCount;

    private Integer status;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
