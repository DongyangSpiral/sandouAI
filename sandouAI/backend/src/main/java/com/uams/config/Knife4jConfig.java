package com.uams.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UAMS 统一认证管理系统 API")
                        .version("1.0.0")
                        .description("DeepSeek 统一认证管理系统接口文档"));
    }
}
