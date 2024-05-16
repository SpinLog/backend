package com.example.spinlog.global.security.oauth2.jwt;

import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import com.example.spinlog.global.security.oauth2.user.OAuth2Response;
import com.example.spinlog.global.security.oauth2.user.impl.GoogleResponse;
import com.example.spinlog.global.security.oauth2.user.impl.KakaoResponse;
import com.example.spinlog.global.security.oauth2.user.impl.NaverResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.spinlog.global.security.oauth2.jwt.JwtUtils.AUTHORIZATION_COOKIE;
import static com.example.spinlog.global.security.oauth2.service.CustomOAuth2UserService.GOOGLE;
import static com.example.spinlog.global.security.oauth2.service.CustomOAuth2UserService.KAKAO;
import static com.example.spinlog.global.security.oauth2.service.CustomOAuth2UserService.NAVER;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT Filter begin: {}", request.getRequestURI());

        String authorization = null;

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AUTHORIZATION_COOKIE)) {
                authorization = cookie.getValue();
            }
        }

        //Authorization 토큰 유무 검증
        if (authorization == null) {
            log.info("Authorization cookie not found");
            filterChain.doFilter(request, response);

            //조건에 해당되면 메서드 종료(필수)
            return;
        }

        //토큰 소멸 시간 검증
        String token = authorization;
        if (jwtUtils.isExpired(token)) {
            log.info("JWT Token expired");
            filterChain.doFilter(request, response);

            //조건에 해당되면 메서드 종료(필수)
            return;
        }
        log.info("JWT Token is alive");

        //SecurityContextHolder 를 이용하여 stateless 세션(해당 요청이 끝날 때까지만 살아있는 세션)에 사용자 등록
        OAuth2Response oAuth2Response = getMatchingOAuth2Response(token);

        CustomOAuth2User customOAuth2User = CustomOAuth2User.builder()
                .oAuth2Response(oAuth2Response)
                .firstLogin(jwtUtils.getFirstLogin(token))
                .build();

        Authentication authToken = new OAuth2AuthenticationToken(
                customOAuth2User, customOAuth2User.getAuthorities(), customOAuth2User.getOAuth2Provider()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("JWT Authentication Success");

        filterChain.doFilter(request, response);
    }

    private OAuth2Response getMatchingOAuth2Response(String token) {
        String provider = jwtUtils.getProvider(token);
        String email = jwtUtils.getEmail(token);
        String providerMemberId = jwtUtils.getProviderMemberId(token);

        Map<String, Object> attribute = new HashMap<>();

        if (provider.equals(KAKAO)) {
            attribute.put("id", providerMemberId);
            attribute.put("kakao_account", Map.of("email", email));

            return KakaoResponse.of(attribute);
        }
        if (provider.equals(NAVER)) {
            attribute.put("response", Map.of(
                    "id", providerMemberId,
                    "email", email
            ));

            return NaverResponse.of(attribute);
        }
        if (provider.equals(GOOGLE)){
            attribute.put("sub", providerMemberId);
            attribute.put("email", email);

            return GoogleResponse.of(attribute);
        }

        throw new OAuth2AuthenticationException("Unsupported OAuth2Provider: " + provider);
    }
}
