package com.example.spinlog.util;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.service.request.ArticleCreateRequest;
import com.example.spinlog.user.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class ArticleFactory {

    private User user;  // 회원

    @Builder.Default
    private String content = "content";

    @Builder.Default
    private LocalDateTime spendDate = LocalDateTime.now();

    @Builder.Default
    private String event = "event";

    @Builder.Default
    private String thought = "thought";

    @Builder.Default
    private Emotion emotion = Emotion.SAD;

    @Builder.Default
    private Float satisfaction = 1.0f;

    @Builder.Default
    private String reason = "reason";

    @Builder.Default
    private String improvements = "improvements";

    @Builder.Default
    private String aiComment = "aiComment";

    @Builder.Default
    private Integer amount = 10000;

    @Builder.Default
    private RegisterType registerType = RegisterType.SPEND;

    public Article createArticle() {
        return Article.builder()
                .user(user)
                .content(content)
                .spendDate(spendDate)
                .event(event)
                .thought(thought)
                .emotion(emotion)
                .satisfaction(satisfaction)
                .reason(reason)
                .improvements(improvements)
                .aiComment(aiComment)
                .amount(amount)
                .registerType(registerType)
                .build();
    }

    public ArticleCreateRequest toCreateRequest() {

        return ArticleCreateRequest.builder()
                .content(content)
                .spendDate(spendDate.toString())
                .event(event)
                .thought(thought)
                .emotion(emotion.name())
                .satisfaction(satisfaction)
                .reason(reason)
                .improvements(improvements)
                .amount(amount)
                .registerType(registerType.name())
                .build();
    }
}
