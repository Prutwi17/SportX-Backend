package com.sportx.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @PostConstruct
    public void validateDatabaseConfiguration() {
        log.info("=================================================");
        log.info("[SportX DB Init] Active JDBC URL: {}", datasourceUrl);
        log.info("[SportX DB Init] Active DB Username: {}", datasourceUsername);
        if (datasourceUrl.contains("localhost") && !"local".equalsIgnoreCase(System.getenv("SPRING_PROFILES_ACTIVE"))) {
            log.warn("[SportX DB ALERT] Connecting to 'localhost' in production! Verify SPRING_DATASOURCE_URL in Render Environment Variables.");
        }
        log.info("=================================================");
    }
}
