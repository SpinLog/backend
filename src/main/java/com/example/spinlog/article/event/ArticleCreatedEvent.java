package com.example.spinlog.article.event;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.user.entity.User;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ArticleCreatedEvent {
    private Article article;
    private User user;
}
