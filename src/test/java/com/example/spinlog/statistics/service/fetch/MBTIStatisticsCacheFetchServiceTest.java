package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.MBTI_SATISFACTION_COUNT_KEY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MBTIStatisticsCacheFetchServiceTest {
    CacheHashRepository cacheHashRepository = mock(CacheHashRepository.class);
    MBTIStatisticsCacheFetchService mbtiStatisticsCacheFetchService =
            new MBTIStatisticsCacheFetchService(cacheHashRepository);

    @Nested
    class getAmountAveragesEachMBTIAndEmotionLast30Days {
        @Test
        void CacheService로부터_합과_개수를_조회한_후_그대로_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Map<String, Object> sumsMap = Map.of(
                    "T::PROUD", 1000L,
                    "T::SAD", 2000L,
                    "F::PROUD", 3000L,
                    "F::SAD", 4000L);
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(sumsMap);
            Map<String, Object> countsMap = Map.of(
                    "T::PROUD", 5L,
                    "T::SAD", 5L,
                    "F::PROUD", 5L,
                    "F::SAD", 5L);
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType))))
                    .thenReturn(countsMap);

            // when
            SumAndCountStatisticsData<Long> results = mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(registerType);

            // then
            assertThat(results.sumData()).isEqualTo(sumsMap);
            assertThat(results.countData()).isEqualTo(countsMap);
        }

        @Test
        void CacheService로부터_받은_데이터를_Long으로_캐스팅하여_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "T::PROUD", "1000",
                            "T::SAD", "2000",
                            "F::PROUD", "3000",
                            "F::SAD", "4000"));
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "T::PROUD", "5",
                            "T::SAD", "5",
                            "F::PROUD", "5",
                            "F::SAD", "5"));

            // when
            SumAndCountStatisticsData<Long> results = mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(registerType);

            // then
            for(var e: results.sumData().entrySet()) {
                assertThat(e.getValue()).isInstanceOf(Long.class);
            }
            for(var e: results.countData().entrySet()) {
                assertThat(e.getValue()).isInstanceOf(Long.class);
            }
        }
    }

    @Nested
    class getAmountSumsEachMBTIAndDayLast30Days{
        @Test
        void CacheService로부터_데이터를_조회한_후_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Map<String, Object> sumsMap = Map.of(
                    "T::2024-07-01", 1000L,
                    "T::2024-07-02", 2000L,
                    "F::2024-07-01", 3000L,
                    "F::2024-07-02", 4000L);
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(sumsMap);

            // when
            Map<String, Long> results = mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(registerType);

            // then
            assertThat(results).isEqualTo(sumsMap);
        }

        @Test
        void CacheService로부터_받은_데이터를_Long으로_캐스팅하여_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "T::2024-07-01", "1000",
                            "T::2024-07-02", "2000",
                            "F::2024-07-01", "3000",
                            "F::2024-07-02", "4000"));

            // when
            Map<String, Long> results = mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(registerType);

            // then
            for(var e: results.entrySet()) {
                assertThat(e.getValue()).isInstanceOf(Long.class);
            }
        }
    }

    @Nested
    class getSatisfactionAveragesEachMBTILast30Days{
        @Test
        void CacheService로부터_합과_개수를_조회한_후_그대로_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Map<String, Object> sumsMap = Map.of(
                    "T", 34.0,
                    "F", 78.0);
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_SATISFACTION_SUM_KEY_NAME(registerType))))
                    .thenReturn(sumsMap);
            Map<String, Object> countsMap = Map.of(
                    "T", 10L,
                    "F", 20L);
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_SATISFACTION_COUNT_KEY_NAME(registerType))))
                    .thenReturn(countsMap);

            // when
            SumAndCountStatisticsData<Double> results = mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(registerType);

            // then
            assertThat(results.sumData()).isEqualTo(sumsMap);
            assertThat(results.countData()).isEqualTo(countsMap);
        }

        @Test
        void CacheService로부터_받은_데이터를_Double과_Long으로_캐스팅하여_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_SATISFACTION_SUM_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "T", "34.0",
                            "F", "78.0"));
            when(cacheHashRepository.getHashEntries(
                    eq(MBTI_SATISFACTION_COUNT_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "T", "10",
                            "F", "20"));

            // when
            SumAndCountStatisticsData<Double> results = mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(registerType);

            // then
            for(var e: results.sumData().entrySet()) {
                assertThat(e.getValue()).isInstanceOf(Double.class);
            }
            for(var e: results.countData().entrySet()) {
                assertThat(e.getValue()).isInstanceOf(Long.class);
            }
        }
    }

}