package com.uams.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dfs")
public class DfsConfig {

    private String type;
    private Local local = new Local();

    @Data
    public static class Local {
        private String path;
        private long maxSize;
    }
}
