package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("u_login_log")
public class ULoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String loginType;

    private String username;

    private String ip;

    private String userAgent;

    private Integer status;

    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
