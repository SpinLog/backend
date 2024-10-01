package com.example.spinlog.article.service;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.event.ArticleCreatedEvent;
import com.example.spinlog.article.event.ArticleDeletedEvent;
import com.example.spinlog.article.event.ArticleUpdatedEvent;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.article.service.request.ArticleCreateRequest;
import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.spinlog.article.entity.Emotion.ANNOYED;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.user.entity.Gender.MALE;
import static com.example.spinlog.user.entity.Mbti.ISTJ;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
class ArticleServicePublishEventTest {

    private ArticleRepository articleRepository = mock(ArticleRepository.class);

    private UserRepository userRepository = mock(UserRepository.class);

    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private ArticleService articleService =
             new ArticleService(articleRepository, userRepository, eventPublisher);
    @Test
    @DisplayName("게시글 생성시 ArticleCreatedEvent가 발행된다.")
    void createArticle_publish_event() throws Exception {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        given(articleRepository.save(any(Article.class))).willReturn(article);

        ArticleCreateRequest requestDto = createArticleCreateRequest();

        // when
        articleService.createArticle(user.getAuthenticationName(), requestDto);

        // then
        verify(eventPublisher).publishEvent(any(ArticleCreatedEvent.class));
    }

    @Test
    @DisplayName("게시글 수정시 ArticleUpdatedEvent가 발행된다.")
    void updateArticle_publish_event() throws Exception {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        ArticleUpdateRequest updateRequest = ArticleUpdateRequest.builder()
                .content("Test Content")
                .spendDate("2024-04-04T11:22:33")
                .event("Test event")
                .thought("Test thought")
                .emotion("ANNOYED")
                .satisfaction(1F)
                .reason("Test Reason")
                .improvements("Test Improvements")
                .amount(100)
                .registerType("SAVE")
                .build();
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));

        // when
        articleService.updateArticle("test user", 1L, updateRequest);

        // then
        verify(eventPublisher).publishEvent(any(ArticleUpdatedEvent.class));
    }

    @Test
    @DisplayName("게시글 삭제시 ArticleDeletedEvent가 발행된다.")
    void deleteArticle_publish_event() throws Exception {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));

        // when
        articleService.deleteArticle("test user", 1L);

        // then
        verify(eventPublisher).publishEvent(any(ArticleDeletedEvent.class));
    }

    private User createUser() {
        return User.builder()
                .email("test@example.com")
                .mbti(ISTJ)
                .gender(MALE)
                .authenticationName("test user")
                .build();
    }

    private Article createArticle(User user) {
        return Article.builder()
                .user(user)
                .content("test content")
                .spendDate(LocalDateTime.of(2024, 5, 30, 0, 0))
                .event("test event")
                .thought("test thought")
                .emotion(ANNOYED)
                .satisfaction(5F)
                .reason(null)
                .improvements(null)
                .aiComment(null)
                .amount(100)
                .registerType(SPEND)
                .build();
    }

    private ArticleCreateRequest createArticleCreateRequest() {
        return ArticleCreateRequest.builder()
                .content("Test Content")
                .spendDate("2024-04-04T11:22:33")
                .event("Test event")
                .thought("Test thought")
                .emotion("ANNOYED")
                .satisfaction(5F)
                .reason("Test Reason")
                .improvements("Test Improvements")
                .amount(100)
                .registerType("SPEND")
                .build();
    }
}