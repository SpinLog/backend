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

    /*
    TODO 유지보수 기간에 하면 좋을 것들 (프론트와 협의)
        1. refresh token 추가
            access token 의 생명주기는 짧게, refresh token 의 생명주기는 길게
        2. access token, refresh token 을 각각 로컬 스토리지(csrf 방지), 쿠키에 저장(csrf 는 의미 없게. httpOnly 설정 -> xss 도 방지)
        3. refresh token rotate
            refresh token 을 사용해 access token 을 재발급할 때 refresh token 또한 같이 재발급.
            즉 refresh token 을 한 번 쓰면 더이상 못 쓰도록 제한
     */

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
