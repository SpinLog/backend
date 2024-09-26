package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.PERIOD_CRITERIA;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheWriterService {
    private final CacheService cacheService;

    public void updateStatisticsCacheFromNewData(Article article, User user) {
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
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(PERIOD_CRITERIA);
        return !(spendDate.isAfter(startDate) && spendDate.isBefore(endDate));
    }

    public void updateStatisticsCacheFromModifiedData(Article originalArticle, Article updateArticle, User user) {
        if(user.getGender() == Gender.NONE)
            return;
        if(isNotChanged(originalArticle, updateArticle))
            return;
        log.info("Update cached data, modified originalArticle id: {}", updateArticle.getArticleId());

        updateStatisticsCacheFromRemovedData(originalArticle, user);
        updateStatisticsCacheFromNewData(updateArticle, user);

    }

    // todo word frequency cache 추가 시 수정 필요
    private boolean isNotChanged(Article originalArticle, Article updateArticle) {
        return originalArticle.getAmount().equals(updateArticle.getAmount()) &&
                originalArticle.getEmotion().equals(updateArticle.getEmotion()) &&
                originalArticle.getSpendDate().equals(updateArticle.getSpendDate()) &&
                originalArticle.getSatisfaction().equals(updateArticle.getSatisfaction());
    }

    public void updateStatisticsCacheFromRemovedData(Article article, User user) {
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
