package com.example.spinlog.util;

import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DataSetupService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    User male, female;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void setUp() {
        saveTwoUsers();
        saveFourArticles();
    }

    private void saveTwoUsers() {
        male = User.builder()
                .email("email1@email")
                .authenticationName("name1")
                .mbti(Mbti.ISTJ)
                .gender(Gender.MALE)
                .build();
        female = User.builder()
                .email("email2@email")
                .authenticationName("name2")
                .mbti(Mbti.ISTP)
                .gender(Gender.FEMALE)
                .build();
        userRepository.save(male);
        userRepository.save(female);
    }

    private void saveFourArticles() {
        User[] users = {male, male, female, female};
        int[] amounts = { 1000, 2000, 3000, 4000 };
        float[] satisfactions = { 1.0f, 2.0f, 3.0f, 4.0f };
        LocalDateTime spendDate = LocalDateTime.now().minusDays(1);

        for (int i = 0; i < 4; i++) {
            articleRepository.save(
                    ArticleFactory.builder()
                            .user(users[i])
                            .amount(amounts[i])
                            .satisfaction(satisfactions[i])
                            .spendDate(spendDate)
                            .build()
                            .createArticle());
        }

    }
}
