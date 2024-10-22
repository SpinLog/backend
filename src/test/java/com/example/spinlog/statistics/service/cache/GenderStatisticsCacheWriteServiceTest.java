package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
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
        SumAndCountStatisticsData<Long> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(Map.of("key1", 1L), Map.of("key2", 2L));

        // when
        genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(sumAndCountStatisticsData, SAVE);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 1L));
        entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key2", 2L));
    }

    @Test
    void putAmountSumsByGenderAndDate() {
        // given
        Map<String, Long> amountSums = Map.of("key1", 1L);

        // when
        genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(amountSums, SPEND);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND));
        assertThat(entries).isEqualTo(Map.of("key1", 1L));
    }

    @Test
    void putSatisfactionCountsAndSumsByGender() {
        // given
        SumAndCountStatisticsData<Double> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(Map.of("key1", 1.0), Map.of("key2", 2L));

        // when
        genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(sumAndCountStatisticsData, SAVE);

        // then
        verify(cacheHashRepository).putAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), sumAndCountStatisticsData.countData());
        verify(cacheHashRepository).putAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), sumAndCountStatisticsData.sumData());
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 1.0));
        entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key2", 2L));
    }

    @Test
    void decrementAllData() {
        // given
        AllStatisticsCacheData statisticsAllData = AllStatisticsCacheData.builder()
                .emotionAmountSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .emotionAmountSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .dailyAmountSaveSums(Map.of())
                .dailyAmountSpendSums(Map.of())
                .satisfactionSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .satisfactionSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .build();

        // when
        genderStatisticsCacheWriteService.decrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.dailyAmountSpendSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().sumData());

        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.dailyAmountSaveSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().sumData());
    }

    @Test
    void incrementAllData() {
        // given
        AllStatisticsCacheData statisticsAllData = AllStatisticsCacheData.builder()
                .emotionAmountSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .emotionAmountSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .dailyAmountSaveSums(Map.of())
                .dailyAmountSpendSums(Map.of())
                .satisfactionSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .satisfactionSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .build();

        // when
        genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.dailyAmountSpendSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().sumData());

        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.dailyAmountSaveSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().sumData());
    }

    @Test
    void replaceAmountCountsAndSumsByGenderAndEmotion() {
        // given
        RegisterType registerType = SAVE;
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                Map.of("randomKey1", 123456L));
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                Map.of("randomKey2", 12345L));
        SumAndCountStatisticsData<Long> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(Map.of("key1", 1L), Map.of("key2", 2L));

        // when
        genderStatisticsCacheWriteService.replaceAmountCountsAndSumsByGenderAndEmotion(sumAndCountStatisticsData, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1L));
        entries = cacheHashRepository.getHashEntries(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key2", 2L));
    }

    @Test
    void replaceAmountSumsByGenderAndDate() {
        // given
        RegisterType registerType = SPEND;
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                Map.of("randomKey1", 123456));
        Map<String, Long> amountSums = Map.of("key1", 1L);

        // when
        genderStatisticsCacheWriteService.replaceAmountSumsByGenderAndDate(amountSums, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1L));
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
        SumAndCountStatisticsData<Double> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(Map.of("key1", 1.0), Map.of("key2", 2L));

        // when
        genderStatisticsCacheWriteService.replaceSatisfactionCountsAndSumsByGender(sumAndCountStatisticsData, registerType);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key1", 1.0));
        entries = cacheHashRepository.getHashEntries(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        assertThat(entries).isEqualTo(Map.of("key2", 2L));
    }

}