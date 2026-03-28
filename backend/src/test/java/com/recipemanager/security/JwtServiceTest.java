package com.recipemanager.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.recipemanager.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void createsAndParsesToken() {
        JwtProperties props = new JwtProperties(
                "recipe-manager-jwt-secret-at-least-256-bits-long-for-hmac-sha256-algorithm", 60_000L);
        JwtService jwt = new JwtService(props);
        String token = jwt.createToken(42L, "u@example.com");
        Claims claims = jwt.parse(token);
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("u@example.com");
    }
}
