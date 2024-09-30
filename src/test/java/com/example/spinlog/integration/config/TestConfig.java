package com.example.spinlog.integration.config;


import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;
    @Bean
    public DataSetupService dataSetupService() {
        return new DataSetupService(articleRepository, userRepository);
    }
}
