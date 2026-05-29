package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_file")
public class TeamFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long folderId;

    private Long fileId;

    private String permission;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
