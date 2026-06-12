package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("u_app")
public class UApp {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appName;

    private String appKey;

    private String appSecret;

    private String description;

    private String redirectUri;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
