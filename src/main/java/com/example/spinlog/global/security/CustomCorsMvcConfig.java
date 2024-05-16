package com.example.spinlog.global.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Configuration
public class CustomCorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .exposedHeaders(SET_COOKIE)
                .allowedOrigins("http://localhost:5173");

//        registry.addMapping("/**")
//                .allowedOrigins("https://frontend-chi-sage-83.vercel.app" , "http://localhost:5173")
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("Content-Type", "Authorization", "Set-Cookie")
//                .allowCredentials(true)
//                .maxAge(3600)
//                .exposedHeaders(HttpHeaders.AUTHORIZATION, HttpHeaders.SET_COOKIE);
    }
}
