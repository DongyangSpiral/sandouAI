package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_team")
public class TeamTeam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private String description;

    private Long ownerId;

    private Integer maxMember;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
