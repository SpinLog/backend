package com.example.spinlog.article.event;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ArticleDeletedEvent {
    private Article article;
    private User user;
}
