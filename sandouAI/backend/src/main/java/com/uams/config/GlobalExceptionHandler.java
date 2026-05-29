package com.uams.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.uams.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        Throwable cause = e.getCause();
        String causeMsg = cause != null ? " | caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage() : "";
        return Result.error(500, e.getClass().getSimpleName() + ": " + e.getMessage() + causeMsg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(500, "服务器内部错误");
    }
}
