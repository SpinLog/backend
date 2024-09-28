package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.event.ArticleCreatedEvent;
import com.example.spinlog.article.event.ArticleDeletedEvent;
import com.example.spinlog.article.event.ArticleUpdatedEvent;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.PERIOD_CRITERIA;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheWriterService { // todo 이름 수정 (Artcile이 업데이트 될 때마다 캐시 업데이트 되는 걸 의미하는 이름으로)
    private final CacheService cacheService;

    @TransactionalEventListener
    public void updateStatisticsCacheFromNewData(ArticleCreatedEvent event) {
        Article article = event.getArticle();
        User user = event.getUser();

        if(isNotInStatisticsPeriodCriteria(article.getSpendDate()))
            return;

        // todo MBTI statistics 추가하면 수정 필요
        if(user.getGender() == Gender.NONE)
            return;
        log.info("Update cached data, new article id: {}", article.getArticleId());

        RegisterType registerType = article.getRegisterType();
        cacheService.incrementDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(registerType),
                user.getGender() + "::" + article.getEmotion(), article.getAmount());
        cacheService.incrementDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(registerType),
                user.getGender() + "::" + article.getEmotion(), 1L);

        cacheService.incrementDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), article.getAmount());

        cacheService.incrementDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(registerType),
                user.getGender().name(), article.getSatisfaction());
        cacheService.incrementDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(registerType),
                user.getGender().name(), 1L);

    }

    private boolean isNotInStatisticsPeriodCriteria(LocalDateTime spendDate) {
        // todo 새벽 시간 캐시 데이터 정합성 해결 위해 별도의 클래스로 분리
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(PERIOD_CRITERIA);
        return !(spendDate.isAfter(startDate) && spendDate.isBefore(endDate));
    }

    @TransactionalEventListener
    public void updateStatisticsCacheFromModifiedData(ArticleUpdatedEvent event) {
        Article originalArticle = event.getOriginalArticle();
        Article updateArticle = event.getUpdatedArticle();
        User user = event.getUser();

        if(user.getGender() == Gender.NONE)
            return;
        if(isNotChanged(originalArticle, updateArticle))
            return;
        log.info("Update cached data, modified originalArticle id: {}", updateArticle.getArticleId());

        // 캐싱 작업을 private 메서드로 선언하여 public 메서드 호출 삭제
        updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(originalArticle, user));
        updateStatisticsCacheFromNewData(new ArticleCreatedEvent(updateArticle, user));

    }

    // todo word frequency cache 추가 시 수정 필요
    private boolean isNotChanged(Article originalArticle, Article updateArticle) {
        return originalArticle.getAmount().equals(updateArticle.getAmount()) &&
                originalArticle.getEmotion().equals(updateArticle.getEmotion()) &&
                originalArticle.getSpendDate().equals(updateArticle.getSpendDate()) &&
                originalArticle.getSatisfaction().equals(updateArticle.getSatisfaction());
    }

    @TransactionalEventListener
    public void updateStatisticsCacheFromRemovedData(ArticleDeletedEvent event) {
        Article article = event.getArticle();
        User user = event.getUser();

        if(isNotInStatisticsPeriodCriteria(article.getSpendDate()))
            return;
        if(user.getGender() == Gender.NONE)
            return;
        log.info("Remove cached data, removed article id: {}", article.getArticleId());

        RegisterType registerType = article.getRegisterType();
        cacheService.decrementDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(registerType),
                user.getGender() + "::" + article.getEmotion(), article.getAmount());
        cacheService.decrementDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(registerType),
                user.getGender() + "::" + article.getEmotion(), 1L);

        cacheService.decrementDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), article.getAmount());

        cacheService.decrementDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(registerType),
                user.getGender().name(), article.getSatisfaction());
        cacheService.decrementDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(registerType),
                user.getGender().name(), 1L);

    }

}
