package com.example.spinlog.article.event;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ArticleUpdatedEvent {
    private Article originalArticle;
    private Article updatedArticle;
    private User user;
}
