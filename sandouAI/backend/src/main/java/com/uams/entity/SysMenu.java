package com.uams.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String menuName;

    private Long parentId;

    private String path;

    private String component;

    private String icon;

    private String menuType;

    private String perms;

    private Integer sortOrder;

    private Integer status;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SysMenu> children;
}
