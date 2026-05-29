package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_log")
public class TeamLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long userId;

    private String action;

    private String targetType;

    private Long targetId;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
