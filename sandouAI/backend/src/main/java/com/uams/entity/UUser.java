package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("u_user")
public class UUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String password;

    private String realName;

    private String idCardType;

    private String idCardNo;

    private String nickname;

    private Integer gender;

    private Integer certLevel;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String phoneMasked;

    @TableField(exist = false)
    private String idCardNoMasked;
}
