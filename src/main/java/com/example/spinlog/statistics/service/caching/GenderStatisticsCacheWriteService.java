package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheWriteService {
    private final HashCacheService hashCacheService;

    // todo test

    public void putAmountCountsAndSumsByGenderAndEmotion(CountsAndSums amountCountsAndSums, RegisterType registerType) {
        hashCacheService.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType), amountCountsAndSums.countsMap());
        hashCacheService.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType), amountCountsAndSums.sumsMap());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        hashCacheService.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(CountsAndSums satisfactionCountsAndSums, RegisterType registerType) {
        hashCacheService.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType), satisfactionCountsAndSums.countsMap());
        hashCacheService.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType), satisfactionCountsAndSums.sumsMap());
    }

    public void decrementAllData(AllStatisticsMap statisticsAllData) {
        hashCacheService.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        hashCacheService.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        hashCacheService.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        hashCacheService.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        hashCacheService.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        hashCacheService.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    public void incrementAllData(AllStatisticsMap statisticsAllData) {
        hashCacheService.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        hashCacheService.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        hashCacheService.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        hashCacheService.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        hashCacheService.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        hashCacheService.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }
}