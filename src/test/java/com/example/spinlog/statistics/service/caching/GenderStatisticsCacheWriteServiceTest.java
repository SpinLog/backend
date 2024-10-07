package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.utils.CacheKeyNameUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GenderStatisticsCacheWriteServiceTest {
    HashCacheService hashCacheService = mock(HashCacheService.class);

    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService =
            new GenderStatisticsCacheWriteService(hashCacheService);

    @Test
    void putAmountCountsAndSumsByGenderAndEmotion() {
        CountsAndSums countsAndSums = new CountsAndSums(Map.of("key1", 1), Map.of("key2", 2));

        genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(countsAndSums, SAVE);

        verify(hashCacheService).putAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), countsAndSums.countsMap());
        verify(hashCacheService).putAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), countsAndSums.sumsMap());
    }

    @Test
    void putAmountSumsByGenderAndDate() {
        Map<String, Object> amountSums = Map.of("key1", 1);

        genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(amountSums, SPEND);

        verify(hashCacheService).putAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), amountSums);
    }

    @Test
    void putSatisfactionCountsAndSumsByGender() {
        CountsAndSums countsAndSums = new CountsAndSums(Map.of("key1", 1), Map.of("key2", 2));

        genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(countsAndSums, SAVE);

        verify(hashCacheService).putAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), countsAndSums.countsMap());
        verify(hashCacheService).putAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), countsAndSums.sumsMap());
    }

    @Test
    void decrementAllData() {
        AllStatisticsMap statisticsAllData = AllStatisticsMap.builder()
                .genderEmotionAmountSaveCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .genderEmotionAmountSpendCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .genderDailyAmountSaveSums(Map.of())
                .genderDailyAmountSpendSums(Map.of())
                .genderSatisfactionSaveCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .genderSatisfactionSpendCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .build();

        genderStatisticsCacheWriteService.decrementAllData(statisticsAllData);

        verify(hashCacheService).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        verify(hashCacheService).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        verify(hashCacheService).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(hashCacheService).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        verify(hashCacheService).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        verify(hashCacheService).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        verify(hashCacheService).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        verify(hashCacheService).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(hashCacheService).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        verify(hashCacheService).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    @Test
    void incrementAllData() {
        AllStatisticsMap statisticsAllData = AllStatisticsMap.builder()
                .genderEmotionAmountSaveCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .genderEmotionAmountSpendCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .genderDailyAmountSaveSums(Map.of())
                .genderDailyAmountSpendSums(Map.of())
                .genderSatisfactionSaveCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .genderSatisfactionSpendCountsAndSums(
                        new CountsAndSums(Map.of(), Map.of()))
                .build();

        genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        verify(hashCacheService).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        verify(hashCacheService).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        verify(hashCacheService).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(hashCacheService).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        verify(hashCacheService).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        verify(hashCacheService).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        verify(hashCacheService).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        verify(hashCacheService).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(hashCacheService).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        verify(hashCacheService).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

}