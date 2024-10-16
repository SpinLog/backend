package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumsAndCounts;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
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
        SumsAndCounts sumsAndCounts = new SumsAndCounts(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(sumsAndCounts, SAVE);

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
        SumsAndCounts sumsAndCounts = new SumsAndCounts(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(sumsAndCounts, SAVE);

        // then
        verify(cacheHashRepository).putAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), sumsAndCounts.countsMap());
        verify(cacheHashRepository).putAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), sumsAndCounts.sumsMap());
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
        entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key2", 2));
    }

    @Test
    void decrementAllData() {
        // given
        AllGenderStatisticsCacheData statisticsAllData = AllGenderStatisticsCacheData.builder()
                .genderEmotionAmountSaveSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .genderEmotionAmountSpendSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .genderDailyAmountSaveSums(Map.of())
                .genderDailyAmountSpendSums(Map.of())
                .genderSatisfactionSaveSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .genderSatisfactionSpendSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .build();

        // when
        genderStatisticsCacheWriteService.decrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().sumsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().sumsMap());

        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().sumsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().countsMap());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().sumsMap());
    }

    @Test
    void incrementAllData() {
        // given
        AllGenderStatisticsCacheData statisticsAllData = AllGenderStatisticsCacheData.builder()
                .genderEmotionAmountSaveSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .genderEmotionAmountSpendSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .genderDailyAmountSaveSums(Map.of())
                .genderDailyAmountSpendSums(Map.of())
                .genderSatisfactionSaveSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .genderSatisfactionSpendSumsAndCounts(
                        new SumsAndCounts(Map.of(), Map.of()))
                .build();

        // when
        genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().sumsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().sumsMap());

        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().sumsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().countsMap());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().sumsMap());
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
        SumsAndCounts sumsAndCounts = new SumsAndCounts(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.replaceAmountCountsAndSumsByGenderAndEmotion(sumsAndCounts, registerType);

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
        SumsAndCounts sumsAndCounts = new SumsAndCounts(Map.of("key1", 1), Map.of("key2", 2));

        // when
        genderStatisticsCacheWriteService.replaceSatisfactionCountsAndSumsByGender(sumsAndCounts, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1));
        entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key2", 2));
    }

}