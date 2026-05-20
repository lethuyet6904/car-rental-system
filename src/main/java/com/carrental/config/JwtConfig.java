package com.carrental.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
public class JwtConfig {
    private String secret;
    private long expiration;

    // ConfigurationProperties cần setter để bind giá trị
    public void setSecret(String secret) { this.secret = secret; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
}