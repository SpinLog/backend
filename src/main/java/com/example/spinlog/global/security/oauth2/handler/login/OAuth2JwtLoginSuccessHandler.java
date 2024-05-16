package com.example.spinlog.global.security.oauth2.handler.login;

import com.example.spinlog.global.security.oauth2.jwt.JwtUtils;
import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.spinlog.global.security.oauth2.jwt.JwtUtils.AUTHORIZATION_COOKIE;

//TODO 이거 완성하면 OAuth2LoginSuccessHandler 삭제
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2JwtLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("login success");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        //jwt 를 생성하기 위한 프로퍼티 획득
        String provider = oAuth2User.getOAuth2Provider();
        String email = oAuth2User.getOAuth2Response().getEmail();
        String providerMemberId = oAuth2User.getOAuth2Response().getProviderId();
        Boolean firstLogin = oAuth2User.getFirstLogin();

        String token = jwtUtils.createJwt(provider, email, providerMemberId, firstLogin, 60 * 60 * 60L);

        log.info("token created: {}", token);

        response.addCookie(createCookie(AUTHORIZATION_COOKIE, token));
        response.sendRedirect("http://localhost:5173/"); //TODO 맞게 변경
//        response.sendRedirect("http://localhost:8080/profile");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        //cookie.setSecure(true);  //TODO 배포 시 주석 해제 (https 통신에서만 사용하도록 제한)
        cookie.setPath("/");
        cookie.setHttpOnly(true); //자바스크립트가 해당 쿠키를 가져가지 못하도록 설정 TODO 이게 있어야 하나

        return cookie;
    }
}
