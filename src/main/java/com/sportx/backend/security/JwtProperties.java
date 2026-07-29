package com.sportx.backend.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sportx.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
}
