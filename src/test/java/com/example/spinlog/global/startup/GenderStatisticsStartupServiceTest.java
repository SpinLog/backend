package com.example.spinlog.global.startup;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.util.MockCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class GenderStatisticsStartupServiceTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    CacheService cacheService = new MockCacheService();

    GenderStatisticsStartupService genderStatisticsStartupService =
            new GenderStatisticsStartupService(cacheService, genderStatisticsRepository);

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
        Map<String, Object> genderEmotionAmountSums = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND));
        assertThat(genderEmotionAmountSums.size()).isEqualTo(4);
        assertThat(genderEmotionAmountSums.get("MALE::PROUD")).isEqualTo(1L);
        assertThat(genderEmotionAmountSums.get("MALE::SAD")).isEqualTo(2L);
        assertThat(genderEmotionAmountSums.get("FEMALE::PROUD")).isEqualTo(3L);
        assertThat(genderEmotionAmountSums.get("FEMALE::SAD")).isEqualTo(4L);

        assertThat(cacheService.getHashEntries(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SAVE)))
                .hasSize(0);

        Map<String, Object> genderEmotionAmountCounts = cacheService.getHashEntries(
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
            assertThat(cacheService.getHashEntries(key).size()).isZero();
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