package com.uams;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.uams.mapper")
public class UamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UamsApplication.class, args);
    }
}
