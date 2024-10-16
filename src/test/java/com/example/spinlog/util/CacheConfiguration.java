package com.example.spinlog.util;

import com.example.spinlog.global.cache.CacheHashRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CacheConfiguration {
    @Bean
    public CacheHashRepository hashCacheService() {
        return new MockCacheHashRepository();
    }
}
