package com.example.spinlog.global.security.utils;

import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getAuthenticationName() {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return oAuth2User.getOAuth2Response().getAuthenticationName();
    }

}
