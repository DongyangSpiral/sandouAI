package com.uams.config;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                     jakarta.servlet.http.HttpServletResponse response,
                                     Object handler) {
                // 直接抛 NotLoginException，由 GlobalExceptionHandler 统一返回 JSON
                StpUtil.checkLogin();
                return true;
            }
        }).addPathPatterns("/api/system/**", "/api/uas/**", "/api/dashboard/**",
                        "/api/monitor/**", "/api/tool/**",
                        "/api/file/**", "/api/folder/**", "/api/share/**")
                .excludePathPatterns("/api/system/login", "/api/uas/auth/**",
                        "/api/share/access/**");
    }
}
