package com.housekeeping.auth.service;

import com.housekeeping.auth.config.JwtProperties;
import com.housekeeping.auth.support.SessionUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId,
                              String phone,
                              String realName,
                              String roleCode,
                              List<String> permissionCodes) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getJwtExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .claim("realName", realName)
                .claim("roleCode", roleCode)
                .claim("permissions", permissionCodes)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public SessionUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Date expiration = claims.getExpiration();
        return new SessionUser(
                Long.valueOf(claims.getSubject()),
                claims.get("phone", String.class),
                claims.get("realName", String.class),
                claims.get("roleCode", String.class),
                extractPermissions(claims.get("permissions", List.class)),
                expiration == null ? 0L : expiration.getTime()
        );
    }

    private List<String> extractPermissions(List<?> rawPermissions) {
        if (rawPermissions == null || rawPermissions.isEmpty()) {
            return List.of();
        }
        return rawPermissions.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
    }
}
