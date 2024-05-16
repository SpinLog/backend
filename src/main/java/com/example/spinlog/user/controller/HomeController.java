package com.example.spinlog.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController { //TODO 배포 시 삭제

    @GetMapping("/")
    public String homePage() {
        return "main route";
    }

}
