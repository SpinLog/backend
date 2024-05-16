package com.example.spinlog.user.controller;

import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProfileController { //TODO 배포 시 삭제

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        log.info("profile page");

        String name = oAuth2User.getName();
        String provider = oAuth2User.getOAuth2Response().getProvider();

        return provider + " " + name;
    }
}
