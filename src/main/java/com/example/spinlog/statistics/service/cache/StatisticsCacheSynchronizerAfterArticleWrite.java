package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.event.ArticleCreatedEvent;
import com.example.spinlog.article.event.ArticleDeletedEvent;
import com.example.spinlog.article.event.ArticleUpdatedEvent;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheSynchronizerAfterArticleWrite {
    private final CacheHashRepository cacheHashRepository;
    private final StatisticsPeriodManager statisticsPeriodManager;

    @TransactionalEventListener
    public void updateStatisticsCacheFromCreatedData(ArticleCreatedEvent event) {
        Article article = event.getArticle();
        User user = event.getUser();

        if(isNotInStatisticsPeriodCriteria(article.getSpendDate()))
            return;

        log.info("Update cached data, new article id: {}", article.getArticleId());
        if(user.getGender() != Gender.NONE){
            updateGenderStatisticsCacheFromCreatedArticle(article, user);
        }
        if(user.getMbti() != Mbti.NONE){

        }

    }

    @TransactionalEventListener
    public void updateStatisticsCacheFromModifiedData(ArticleUpdatedEvent event) {
        Article originalArticle = event.getOriginalArticle();
        Article updateArticle = event.getUpdatedArticle();
        User user = event.getUser();

        if(isNotChanged(originalArticle, updateArticle))
            return;

        log.info("Update cached data, modified originalArticle id: {}", updateArticle.getArticleId());
        if(user.getGender() != Gender.NONE){
            updateGenderStatisticsCacheFromRemovedArticle(originalArticle, user);
            updateGenderStatisticsCacheFromCreatedArticle(updateArticle, user);
        }
        if(user.getMbti() != Mbti.NONE){

        }

    }

    // todo word frequency cache 추가 시 수정 필요
    @TransactionalEventListener
    public void updateStatisticsCacheFromRemovedData(ArticleDeletedEvent event) {
        Article article = event.getArticle();
        User user = event.getUser();

        if(isNotInStatisticsPeriodCriteria(article.getSpendDate()))
            return;

        log.info("Remove cached data, removed article id: {}", article.getArticleId());
        if(user.getGender() != Gender.NONE){
            updateGenderStatisticsCacheFromRemovedArticle(article, user);
        }
        if(user.getMbti() != Mbti.NONE){

        }
    }

    private boolean isNotInStatisticsPeriodCriteria(LocalDateTime spendDate) {
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDateTime endDate = period.endDate().atStartOfDay();
        LocalDateTime startDate = period.startDate().atStartOfDay();
        return !(spendDate.isAfter(startDate) && spendDate.isBefore(endDate));
    }

    private boolean isNotChanged(Article originalArticle, Article updateArticle) {
        return originalArticle.getAmount().equals(updateArticle.getAmount()) &&
                originalArticle.getEmotion().equals(updateArticle.getEmotion()) &&
                originalArticle.getSpendDate().equals(updateArticle.getSpendDate()) &&
                originalArticle.getSatisfaction().equals(updateArticle.getSatisfaction());
    }

    private void updateGenderStatisticsCacheFromCreatedArticle(Article article, User user) {
        RegisterType registerType = article.getRegisterType();
        cacheHashRepository.incrementDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), article.getAmount());
        cacheHashRepository.incrementDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), 1L);

        cacheHashRepository.incrementDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), article.getAmount());

        cacheHashRepository.incrementDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                user.getGender().name(), article.getSatisfaction());
        cacheHashRepository.incrementDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                user.getGender().name(), 1L);
    }

    private void updateGenderStatisticsCacheFromRemovedArticle(Article article, User user) {
        RegisterType registerType = article.getRegisterType();
        cacheHashRepository.decrementDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), article.getAmount());
        cacheHashRepository.decrementDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), 1L);

        cacheHashRepository.decrementDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), article.getAmount());

        cacheHashRepository.decrementDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                user.getGender().name(), article.getSatisfaction());
        cacheHashRepository.decrementDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                user.getGender().name(), 1L);
    }
}
