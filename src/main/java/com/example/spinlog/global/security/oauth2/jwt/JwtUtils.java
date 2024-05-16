package com.example.spinlog.global.security.oauth2.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

//version: jjwt 0.12.3 기준
@Component
public class JwtUtils {

    public static final String AUTHORIZATION_COOKIE = "Authorization";

    private SecretKey secretKey;

    public JwtUtils(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    public String createJwt(String provider, String email, String providerMemberId, Boolean firstLogin, Long expiredMs) {
        return Jwts.builder()
                .claim("provider", provider)
                .claim("email", email)
                .claim("providerMemberId", providerMemberId)
                .claim("firstLogin", firstLogin)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String getProvider(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("provider", String.class);
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("email", String.class);
    }

    public String getProviderMemberId(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("providerMemberId", String.class);
    }

    public Boolean getFirstLogin(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .get("firstLogin", Boolean.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()
                .getExpiration().before(new Date());
    }
}
