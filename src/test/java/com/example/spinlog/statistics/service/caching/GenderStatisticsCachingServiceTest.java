package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.util.MockCacheService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCachingServiceTest {
    CacheService cacheService = mock(CacheService.class);
    GenderStatisticsCachingService genderStatisticsCachingService =
            new GenderStatisticsCachingService(cacheService);

    @Nested
    class getAmountAveragesEachGenderAndEmotionLast30Days {
        @Test
        void CacheService로부터_합과_개수를_조회한_후_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 1000L,
                            "MALE::SAD", 2000L,
                            "FEMALE::PROUD", 3000L,
                            "FEMALE::SAD", 4000L));
            when(cacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 5L,
                            "MALE::SAD", 5L,
                            "FEMALE::PROUD", 5L,
                            "FEMALE::SAD", 5L));

            // when
            List<GenderEmotionAmountAverageDto> results = genderStatisticsCachingService.getAmountAveragesEachGenderAndEmotionLast30Days(registerType);

            // then
            for(GenderEmotionAmountAverageDto result : results) {
                switch(result.getGender() + "::" + result.getEmotion()) {
                    case "MALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(200L);
                        break;
                    case "MALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(400L);
                        break;
                    case "FEMALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(600L);
                        break;
                    case "FEMALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(800L);
                        break;
                }
            }
        }
    }

    @Nested
    class getAmountSumsEachGenderAndDayLast30Days{
        @Test
        void CacheService로부터_데이터를_조회한_후_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheService.getHashEntries(
                    eq(getGenderDailyStatisticsAmountSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::2024-07-01", 1000L,
                            "MALE::2024-07-02", 2000L,
                            "FEMALE::2024-07-01", 3000L,
                            "FEMALE::2024-07-02", 4000L));
            
            // when
            List<GenderDailyAmountSumDto> results = genderStatisticsCachingService.getAmountSumsEachGenderAndDayLast30Days(registerType);

            // then
            for(GenderDailyAmountSumDto result : results) {
                switch (result.getGender() + "::" + result.getLocalDate()) {
                    case "MALE::2024-07-01":
                        assertThat(result.getAmountSum()).isEqualTo(1000L);
                        break;
                    case "MALE::2024-07-02":
                        assertThat(result.getAmountSum()).isEqualTo(2000L);
                        break;
                    case "FEMALE::2024-07-01":
                        assertThat(result.getAmountSum()).isEqualTo(3000L);
                        break;
                    case "FEMALE::2024-07-02":
                        assertThat(result.getAmountSum()).isEqualTo(4000L);
                        break;
                }
            }
        }
    }

    @Nested
    class getSatisfactionAveragesEachGenderLast30Days{
        @Test
        void CacheService로부터_합과_개수를_조회한_후_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(cacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 12.0,
                            "MALE::SAD", 34.0,
                            "FEMALE::PROUD", 56.0,
                            "FEMALE::SAD", 78.0));
            when(cacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 5L,
                            "MALE::SAD", 10L,
                            "FEMALE::PROUD", 12L,
                            "FEMALE::SAD", 20L));

            // when
            List<GenderEmotionAmountAverageDto> results = genderStatisticsCachingService.getAmountAveragesEachGenderAndEmotionLast30Days(registerType);

            // then
            for(GenderEmotionAmountAverageDto result : results) {
                switch(result.getGender() + "::" + result.getEmotion()) {
                    case "MALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(12.0/5);
                        break;
                    case "MALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(34.0/10);
                        break;
                    case "FEMALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(56.0/12);
                        break;
                    case "FEMALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(78.0/20);
                        break;
                }
            }
        }
    }
}