package com.trikaar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Type-safe configuration properties for TRIKAAR security settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "trikaar.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secretKey;
        private long accessTokenExpirationMs = 3600000; // 1 hour
        private long refreshTokenExpirationMs = 604800000; // 7 days
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private long maxAge = 3600;
    }
}
