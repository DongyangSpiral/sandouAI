package com.uams.common;

import cn.dev33.satoken.stp.StpUtil;

public class AuthUtil {
    public static long getUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (NumberFormatException e) {
            // 处理 admin 用户使用用户名登录时引发的类型转换异常
            // 默认返回 1L 作为系统管理员的拥有者 ID
            return 1L;
        }
    }
}
