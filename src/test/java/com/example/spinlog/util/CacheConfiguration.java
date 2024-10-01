package com.example.spinlog.util;

import com.example.spinlog.global.cache.HashCacheService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CacheConfiguration {
    @Bean
    public HashCacheService hashCacheService() {
        return new MockHashCacheService();
    }
}
