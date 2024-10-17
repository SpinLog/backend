package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.GENDER_EMOTION_AMOUNT_SUM_KEY_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheFetchServiceTest {
    CacheHashRepository cacheHashRepository = mock(CacheHashRepository.class);
    GenderStatisticsCacheFetchService genderStatisticsCacheFetchService =
            new GenderStatisticsCacheFetchService(cacheHashRepository);

    @Nested
    class getAmountAveragesEachGenderAndEmotionLast30Days {
        @Test
        void CacheService로부터_합과_개수를_조회한_후_그대로_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Map<String, Object> sumsMap = Map.of(
                    "MALE::PROUD", 1000L,
                    "MALE::SAD", 2000L,
                    "FEMALE::PROUD", 3000L,
                    "FEMALE::SAD", 4000L);
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(sumsMap);
            Map<String, Object> countsMap = Map.of(
                    "MALE::PROUD", 5L,
                    "MALE::SAD", 5L,
                    "FEMALE::PROUD", 5L,
                    "FEMALE::SAD", 5L);
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType))))
                    .thenReturn(countsMap);

            // when
            SumAndCountStatisticsData results = genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(registerType);

            // then
            assertThat(results.sumData()).isEqualTo(sumsMap);
            assertThat(results.countData()).isEqualTo(countsMap);
        }

        @Test
        void CacheService로부터_받은_데이터를_Long으로_캐스팅하여_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", "1000",
                            "MALE::SAD", "2000",
                            "FEMALE::PROUD", "3000",
                            "FEMALE::SAD", "4000"));
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", "5",
                            "MALE::SAD", "5",
                            "FEMALE::PROUD", "5",
                            "FEMALE::SAD", "5"));

            // when
            SumAndCountStatisticsData results = genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(registerType);

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
    class getAmountSumsEachGenderAndDayLast30Days{
        @Test
        void CacheService로부터_데이터를_조회한_후_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Map<String, Object> sumsMap = Map.of(
                    "MALE::2024-07-01", 1000L,
                    "MALE::2024-07-02", 2000L,
                    "FEMALE::2024-07-01", 3000L,
                    "FEMALE::2024-07-02", 4000L);
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(sumsMap);
            
            // when
            Map<String, Object> results = genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(registerType);

            // then
            assertThat(results).isEqualTo(sumsMap);
        }

        @Test
        void CacheService로부터_받은_데이터를_Long으로_캐스팅하여_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "MALE::2024-07-01", "1000",
                            "MALE::2024-07-02", "2000",
                            "FEMALE::2024-07-01", "3000",
                            "FEMALE::2024-07-02", "4000"));

            // when
            Map<String, Object> results = genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(registerType);

            // then
            for(var e: results.entrySet()) {
                assertThat(e.getValue()).isInstanceOf(Long.class);
            }
        }
    }

    @Nested
    class getSatisfactionAveragesEachGenderLast30Days{
        @Test
        void CacheService로부터_합과_개수를_조회한_후_그대로_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Map<String, Object> sumsMap = Map.of(
                    "MALE", 34.0,
                    "FEMALE", 78.0);
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_SATISFACTION_SUM_KEY_NAME(registerType))))
                    .thenReturn(sumsMap);
            Map<String, Object> countsMap = Map.of(
                    "MALE", 10L,
                    "FEMALE", 20L);
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType))))
                    .thenReturn(countsMap);

            // when
            SumAndCountStatisticsData results = genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(registerType);

            // then
            assertThat(results.sumData()).isEqualTo(sumsMap);
            assertThat(results.countData()).isEqualTo(countsMap);
        }

        @Test
        void CacheService로부터_받은_데이터를_Double과_Long으로_캐스팅하여_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_SATISFACTION_SUM_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "MALE", "34.0",
                            "FEMALE", "78.0"));
            when(cacheHashRepository.getHashEntries(
                    eq(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType))))
                    .thenReturn(Map.of(
                            "MALE", "10",
                            "FEMALE", "20"));

            // when
            SumAndCountStatisticsData results = genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(registerType);

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