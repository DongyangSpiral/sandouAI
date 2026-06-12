package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("u_corp_user")
public class UCorpUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String corpName;

    private String creditCode;

    private String phone;

    private String password;

    private String contactName;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
