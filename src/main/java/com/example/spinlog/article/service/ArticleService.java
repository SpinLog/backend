package com.example.spinlog.article.service;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.article.service.request.ArticleCreateRequest;
import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.article.service.request.SearchCond;
import com.example.spinlog.article.service.response.ViewArticleResponseDto;
import com.example.spinlog.article.service.response.ViewArticleSumDto;
import com.example.spinlog.article.service.response.ViewListResponseDto;
import com.example.spinlog.article.service.response.WriteArticleResponseDto;
import com.example.spinlog.global.error.exception.article.ArticleNotFoundException;
import com.example.spinlog.global.error.exception.user.UnauthorizedArticleRequestException;
import com.example.spinlog.global.error.exception.user.UserNotFoundException;
import com.example.spinlog.statistics.service.caching.GenderStatisticsCacheWriterService;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final GenderStatisticsCacheWriterService genderStatisticsCacheWriterService;

    @Transactional
    public WriteArticleResponseDto createArticle(String userName, ArticleCreateRequest requestDto) {
        User user = getUser(userName);
        Article articleEntity = requestDto.toEntity(user);
        Article savedArticle = articleRepository.save(articleEntity);
        user.addArticle(savedArticle);

        // todo update cache after transaction, after checking jpa transaction strategy
        genderStatisticsCacheWriterService.updateStatisticsCacheFromNewData(savedArticle, user);

        log.info("게시글이 성공적으로 저장되었습니다. ID: {}", savedArticle.getArticleId());
        return WriteArticleResponseDto.from(savedArticle);
    }

    public ViewListResponseDto listArticles(String userName, Pageable pageable, SearchCond searchCond) {
        User user = getUser(userName);
        Page<ViewArticleSumDto> response = articleRepository.search(user, pageable, searchCond);
        return ViewListResponseDto.builder()
                .nextPage(!response.isLast())
                .spendList(response.getContent())
                .build();
    }

    public ViewArticleResponseDto getArticle(String userName, Long id) {
        User user = getUser(userName);
        Article viewArticle = findArticleById(id);
        validateUserArticle(user, viewArticle);
        return ViewArticleResponseDto.from(viewArticle);
    }

    @Transactional
    public void updateArticle(String userName, Long id, ArticleUpdateRequest requestDto) {
        User user = getUser(userName);
        Article updateArticle = findArticleById(id);
        validateUserArticle(user, updateArticle);

        // copy article entity
        Article originalArticle = updateArticle.copyEntity();

        updateArticle.update(requestDto);

        genderStatisticsCacheWriterService.updateStatisticsCacheFromModifiedData(originalArticle, updateArticle, user);

        log.info("ID {}의 게시글이 업데이트되었습니다.", id);
    }

    @Transactional
    public void deleteArticle(String userName, Long id) {
        User user = getUser(userName);
        Article deleteArticle = findArticleById(id);
        validateUserArticle(user, deleteArticle);
        articleRepository.delete(deleteArticle);
        user.removeArticle(deleteArticle);

        genderStatisticsCacheWriterService.updateStatisticsCacheFromRemovedData(deleteArticle, user);

        log.info("ID {}의 게시글이 성공적으로 삭제되었습니다.", id);
    }

    public Article findArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(String.valueOf(id)));
    }

    private User getUser(String userName) {
        return userRepository.findByAuthenticationName(userName)
                .orElseThrow(() -> new UserNotFoundException(userName));
    }

    private void validateUserArticle(User user, Article article) {
        if (!article.getUser().equals(user)) {
            throw new UnauthorizedArticleRequestException("해당 게시글에 대한 권한이 없습니다.");
        }
    }
}
