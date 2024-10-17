package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
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
        AllGenderStatisticsCacheData statisticsAllData = AllGenderStatisticsCacheData.builder()
                .genderEmotionAmountSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .genderEmotionAmountSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .genderDailyAmountSaveSums(Map.of())
                .genderDailyAmountSpendSums(Map.of())
                .genderSatisfactionSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .genderSatisfactionSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .build();

        // when
        genderStatisticsCacheWriteService.decrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().sumData());

        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).decrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().sumData());
    }

    @Test
    void incrementAllData() {
        // given
        AllGenderStatisticsCacheData statisticsAllData = AllGenderStatisticsCacheData.builder()
                .genderEmotionAmountSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .genderEmotionAmountSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .genderDailyAmountSaveSums(Map.of())
                .genderDailyAmountSpendSums(Map.of())
                .genderSatisfactionSaveSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .genderSatisfactionSpendSumAndCountStatisticsData(
                        new SumAndCountStatisticsData<>(Map.of(), Map.of()))
                .build();

        // when
        genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        // then
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().sumData());

        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().sumData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().countData());
        verify(cacheHashRepository).incrementAllDataInHash(GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().sumData());
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