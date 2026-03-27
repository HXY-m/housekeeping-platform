package com.housekeeping.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security")
public class JwtProperties {

    @NotBlank
    private String jwtSecret;

    @Min(1)
    private long jwtExpireHours = 12;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpireHours() {
        return jwtExpireHours;
    }

    public void setJwtExpireHours(long jwtExpireHours) {
        this.jwtExpireHours = jwtExpireHours;
    }
}
