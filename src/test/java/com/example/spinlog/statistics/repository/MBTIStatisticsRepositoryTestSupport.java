package com.example.spinlog.statistics.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class MBTIStatisticsRepositoryTestSupport {

    @Component
    @RequiredArgsConstructor
    public static class ComponentToCreateUserMBTIView {
        private final DataSource dataSource;

        /**
         * JPA에서 등록된 엔티티를 테이블로 생성 해주지만, 뷰는 생성 안되서, sql 파일로 직접 생성
         * */
        @EventListener(ApplicationReadyEvent.class)
        @Order(1)
        public void createUserMBTIView() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("view/before-test-schema.sql"));
            populator.execute(dataSource);
        }
    }
}
