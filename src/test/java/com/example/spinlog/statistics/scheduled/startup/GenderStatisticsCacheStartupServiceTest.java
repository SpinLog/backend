package com.example.spinlog.statistics.scheduled.startup;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.GENDER_SATISFACTION_COUNT_KEY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenderStatisticsCacheStartupServiceTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository);
    CacheHashRepository cacheHashRepository = new MockCacheHashRepository();

    // todo set fixed clock and test
    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    GenderStatisticsCacheStartupService genderStatisticsStartupService =
            new GenderStatisticsCacheStartupService(cacheHashRepository, genderStatisticsRepositoryFetchService, statisticsPeriodManager);



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
        List<String> keys = List.of("MALE::PROUD", "MALE::SAD", "FEMALE::PROUD", "FEMALE::SAD");
        List<Long> amounts = List.of(1L, 2L, 3L, 4L);
        Map<String, Object> genderEmotionAmountSums = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SPEND));
        for(var key: keys){
            assertThat(genderEmotionAmountSums.get(key)).isNotNull();
            assertThat(genderEmotionAmountSums.get(key)).isInstanceOf(Long.class)
                    .isEqualTo(amounts.get(keys.indexOf(key)));
        }

        Map<String, Object> genderEmotionAmountCounts = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SPEND));

        for (var key: keys) {
            assertThat(genderEmotionAmountCounts.get(key)).isNotNull();
            assertThat(genderEmotionAmountCounts.get(key)).isInstanceOf(Long.class)
                    .isEqualTo(1L);
        }
    }

    @Test
    @DisplayName("레포지토리로부터 통계 데이터를 받아 캐시에 제로 패딩을 한 후 저장한다.")
    void startup_test_zero_padding() throws Exception {
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
        List<String> keys = List.of("MALE::PROUD", "MALE::SAD", "FEMALE::PROUD", "FEMALE::SAD");
        Map<String, Object> genderEmotionAmountSums = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SPEND));
        for(var key: genderEmotionAmountSums.keySet()){
            if(keys.contains(key)) continue;
            assertThat(genderEmotionAmountSums.get(key)).isEqualTo(0L);
        }

        Map<String, Object> genderEmotionAmountCounts = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SPEND));
        for(var key: genderEmotionAmountCounts.keySet()){
            if(keys.contains(key)) continue;
            assertThat(genderEmotionAmountSums.get(key)).isEqualTo(0L);
        }

        for(var key: getAllCacheKeyNames()) {
            if(key.equals(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SPEND)) ||
                    key.equals(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SPEND))) continue;
            Map<String, Object> entries = cacheHashRepository.getHashEntries(key);
            for(var e: entries.entrySet()){
                if(e.getValue() instanceof Double){
                    assertThat(e.getValue()).isEqualTo(0.0);
                }
                else
                    assertThat(e.getValue()).isEqualTo(0L);
            }
        }
    }

    private String[] getAllCacheKeyNames(){
        return new String[]{
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SPEND),
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SPEND),
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SAVE),
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SAVE),
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(RegisterType.SPEND),
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(RegisterType.SAVE),
                GENDER_SATISFACTION_SUM_KEY_NAME(RegisterType.SPEND),
                GENDER_SATISFACTION_COUNT_KEY_NAME(RegisterType.SPEND),
                GENDER_SATISFACTION_SUM_KEY_NAME(RegisterType.SAVE),
                GENDER_SATISFACTION_COUNT_KEY_NAME(RegisterType.SAVE)
        };
    }

}