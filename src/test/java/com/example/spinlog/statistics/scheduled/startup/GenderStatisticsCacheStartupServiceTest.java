package com.example.spinlog.statistics.scheduled.startup;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.util.MockHashCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.getGenderStatisticsSatisfactionCountKeyName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenderStatisticsCacheStartupServiceTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository);
    HashCacheService hashCacheService = new MockHashCacheService();
    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    GenderStatisticsCacheStartupService genderStatisticsStartupService =
            new GenderStatisticsCacheStartupService(hashCacheService, genderStatisticsRepositoryFetchService, statisticsPeriodManager);

    @Test
    @DisplayName("레포지토리로부터 통계 데이터를 받아 캐시에 저장한다.")
    void startup_test() throws Exception {
        // given
        List<GenderEmotionAmountAverageDto> returned = List.of(
                new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.PROUD, 1L),
                new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 2L),
                new GenderEmotionAmountAverageDto(Gender.FEMALE, Emotion.PROUD, 3L),
                new GenderEmotionAmountAverageDto(Gender.FEMALE, Emotion.SAD, 4L)
        );
        List<GenderEmotionAmountAverageDto> counts = List.of(
                new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.PROUD, 1L),
                new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 1L),
                new GenderEmotionAmountAverageDto(Gender.FEMALE, Emotion.PROUD, 1L),
                new GenderEmotionAmountAverageDto(Gender.FEMALE, Emotion.SAD, 1L)
        );

        when(genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(eq(RegisterType.SPEND), any() , any()))
                .thenReturn(returned);
        when(genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(eq(RegisterType.SPEND), any() , any()))
                .thenReturn(counts);

        // when
        genderStatisticsStartupService.initGenderStatisticsCache();

        // then
        Map<String, Object> genderEmotionAmountSums = hashCacheService.getHashEntries(
                getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND));
        assertThat(genderEmotionAmountSums.size()).isEqualTo(4);
        assertThat(genderEmotionAmountSums.get("MALE::PROUD")).isEqualTo(1L);
        assertThat(genderEmotionAmountSums.get("MALE::SAD")).isEqualTo(2L);
        assertThat(genderEmotionAmountSums.get("FEMALE::PROUD")).isEqualTo(3L);
        assertThat(genderEmotionAmountSums.get("FEMALE::SAD")).isEqualTo(4L);

        assertThat(hashCacheService.getHashEntries(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SAVE)))
                .hasSize(0);

        Map<String, Object> genderEmotionAmountCounts = hashCacheService.getHashEntries(
                getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND));
        assertThat(genderEmotionAmountCounts.size()).isEqualTo(4);
        assertThat(genderEmotionAmountCounts.get("MALE::PROUD")).isEqualTo(1L);
        assertThat(genderEmotionAmountCounts.get("MALE::SAD")).isEqualTo(1L);
        assertThat(genderEmotionAmountCounts.get("FEMALE::PROUD")).isEqualTo(1L);
        assertThat(genderEmotionAmountCounts.get("FEMALE::SAD")).isEqualTo(1L);

        for (String key : getAllCacheKeyNames()) {
            if (key.equals(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND)) ||
                    key.equals(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND)))
                continue;
            assertThat(hashCacheService.getHashEntries(key).size()).isZero();
        }

    }

    private String[] getAllCacheKeyNames(){
        return new String[]{
                getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND),
                getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND),
                getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SAVE),
                getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SAVE),
                getGenderDailyStatisticsAmountSumKeyName(RegisterType.SPEND),
                getGenderDailyStatisticsAmountSumKeyName(RegisterType.SAVE),
                getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND),
                getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND),
                getGenderStatisticsSatisfactionSumKeyName(RegisterType.SAVE),
                getGenderStatisticsSatisfactionCountKeyName(RegisterType.SAVE)
        };
    }

}