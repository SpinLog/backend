package com.example.spinlog.article.entity;

import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.global.entity.BaseTimeEntity;
import com.example.spinlog.user.entity.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId; // 일기 번호

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 회원

    @NotEmpty
    private String content;  // 내용

    @NotNull
    private LocalDateTime spendDate;    // 소비 날짜

    @Nullable
    private String event; // 사건

    @Nullable
    private String thought; // 생각

    @Enumerated(EnumType.STRING)
    @NotNull
    private Emotion emotion; // 감정

    @NotNull
    private Float satisfaction; // 만족도

    @Nullable
    private String reason; // 이유

    @Nullable
    private String improvements; // 개선점

    @Nullable
    private String aiComment; // AI 한마디

    @NotNull
    private Integer amount; // 금액

    @Enumerated(EnumType.STRING)
    @NotNull
    private RegisterType registerType; // 지출과 소비

    @Builder
    private Article(User user, String content, LocalDateTime spendDate, String event, String thought, Emotion emotion, Float satisfaction, String reason, String improvements, @Nullable String aiComment, Integer amount, RegisterType registerType) {
        this.user = user;
        this.content = content;
        this.spendDate = spendDate;
        this.event = event;
        this.thought = thought;
        this.emotion = emotion;
        this.satisfaction = satisfaction;
        this.reason = reason;
        this.improvements = improvements;
        this.aiComment = aiComment;
        this.amount = amount;
        this.registerType = registerType;
    }

    public void update(ArticleUpdateRequest updateArticle) {
        this.content = updateArticle.getContent();
        this.spendDate = LocalDateTime.parse(updateArticle.getSpendDate());
        this.event = updateArticle.getEvent();
        this.thought = updateArticle.getThought();
        this.emotion = Emotion.valueOf(updateArticle.getEmotion());
        this.satisfaction = updateArticle.getSatisfaction();
        this.reason = updateArticle.getReason();
        this.improvements = updateArticle.getImprovements();
        this.amount = updateArticle.getAmount();
        this.registerType = RegisterType.valueOf(updateArticle.getRegisterType());
    }

    public void addAiComment(String aiComment) {
        this.aiComment = aiComment;
    }

    public Article copyEntity(){
        Article article = Article.builder()
                .user(this.user)
                .content(this.content)
                .spendDate(this.spendDate)
                .event(this.event)
                .thought(this.thought)
                .emotion(this.emotion)
                .satisfaction(this.satisfaction)
                .reason(this.reason)
                .improvements(this.improvements)
                .aiComment(this.aiComment)
                .amount(this.amount)
                .registerType(this.registerType)
                .build();
        article.articleId = this.articleId;
        return article;
    }
}
