package com.example.spinlog.global.security.oauth2.handler.login;

import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
//@RequiredArgsConstructor
//@Slf4j
public class OAuth2LoginSuccessHandler /*implements AuthenticationSuccessHandler*/ {

//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        //response.sendRedirect("/api/authentication/login-result");
//        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
//        Boolean isFirstLogin = principal.getFirstLogin();
//        if(isFirstLogin)
//            response.sendRedirect("http://localhost:5173?isFirstLogin=true");
//        else
//            response.sendRedirect("http://localhost:5173");
//    }
}
