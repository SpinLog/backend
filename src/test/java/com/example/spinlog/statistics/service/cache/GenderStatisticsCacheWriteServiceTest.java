package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllStatisticsMap;
import com.example.spinlog.statistics.dto.cache.CountsAndSums;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class GenderStatisticsCacheWriteServiceTest {
    CacheHashRepository cacheHashRepository = spy(MockCacheHashRepository.class);

    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService =
            new GenderStatisticsCacheWriteService(cacheHashRepository);

    @Test
    void putAmountCountsAndSumsByGenderAndEmotion() {
        // given
        CountsAndSums countsAndSums = new CountsAndSums(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(countsAndSums, SAVE);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
        entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key2", 2));
    }

    @Test
    void putAmountSumsByGenderAndDate() {
        // given
        Map<String, Object> amountSums = Map.of("key1", 1);

        // when
        genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(amountSums, SPEND);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
    }

    @Test
    void putSatisfactionCountsAndSumsByGender() {
        // given
        CountsAndSums countsAndSums = new CountsAndSums(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(countsAndSums, SAVE);

        // then
        verify(cacheHashRepository).putAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), countsAndSums.countsMap());
        verify(cacheHashRepository).putAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), countsAndSums.sumsMap());
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
        entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key2", 2));
    }

    @Test
    void decrementAllData() {
        // given
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

        // when
        genderStatisticsCacheWriteService.decrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    @Test
    void incrementAllData() {
        // given
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

        // when
        genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    @Test
    void replaceAmountCountsAndSumsByGenderAndEmotion() {
        // given
        RegisterType registerType = SAVE;
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                Map.of("randomKey1", 123456));
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                Map.of("randomKey2", 12345));
        CountsAndSums countsAndSums = new CountsAndSums(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.replaceAmountCountsAndSumsByGenderAndEmotion(countsAndSums, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
        entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key2", 2));
    }

    @Test
    void replaceAmountSumsByGenderAndDate() {
        // given
        RegisterType registerType = SPEND;
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                Map.of("randomKey1", 123456));
        Map<String, Object> amountSums = Map.of("key1", 1);

        // when
        genderStatisticsCacheWriteService.replaceAmountSumsByGenderAndDate(amountSums, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
    }

    @Test
    void replaceSatisfactionCountsAndSumsByGender() {
        // given
        RegisterType registerType = SAVE;
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                Map.of("randomKey1", 123456));
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                Map.of("randomKey2", 12345));
        CountsAndSums countsAndSums = new CountsAndSums(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.replaceSatisfactionCountsAndSumsByGender(countsAndSums, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
        entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key2", 2));
    }

}