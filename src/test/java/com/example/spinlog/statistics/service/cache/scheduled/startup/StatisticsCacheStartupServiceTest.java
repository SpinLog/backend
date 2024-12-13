package com.example.spinlog.statistics.service.cache.scheduled.startup;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.GenderEmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.MBTIEmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import com.example.spinlog.statistics.repository.SpecificUserStatisticsRepository;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

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

@ActiveProfiles("test")
class StatisticsCacheStartupServiceTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    MBTIStatisticsRepository mbtiStatisticsRepository = mock(MBTIStatisticsRepository.class);
    SpecificUserStatisticsRepository specificUserStatisticsRepository = mock(SpecificUserStatisticsRepository.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository, specificUserStatisticsRepository);
    MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService =
            new MBTIStatisticsRepositoryFetchService(mbtiStatisticsRepository, specificUserStatisticsRepository);
    CacheHashRepository cacheHashRepository = new MockCacheHashRepository();

    // todo set fixed clock and test
    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    StatisticsCacheStartupService targetService =
            new StatisticsCacheStartupService(cacheHashRepository, genderStatisticsRepositoryFetchService, mbtiStatisticsRepositoryFetchService, statisticsPeriodManager);



    @Test
    @DisplayName("레포지토리로부터 성별 통계 데이터를 받아 캐시에 저장한다.")
    void startup_gender_test() throws Exception {
        // given
        List<GenderEmotionAmountSumAndCountDto> returned = List.of(
                new GenderEmotionAmountSumAndCountDto(Gender.MALE, Emotion.PROUD, 1L, 1L),
                new GenderEmotionAmountSumAndCountDto(Gender.MALE, Emotion.SAD, 2L, 1L),
                new GenderEmotionAmountSumAndCountDto(Gender.FEMALE, Emotion.PROUD, 3L, 1L),
                new GenderEmotionAmountSumAndCountDto(Gender.FEMALE, Emotion.SAD, 4L, 1L)
        );

        List<GenderEmotionAmountSumAndCountDto> dtos = genderStatisticsRepository
                .getAmountSumsAndCountsEachGenderAndEmotionBetweenStartDateAndEndDate(eq(RegisterType.SPEND), any(), any());

        when(dtos).thenReturn(returned);

        // when
        targetService.initStatisticsCache();

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
    @DisplayName("레포지토리로부터 MBTI 통계 데이터를 받아 캐시에 저장한다.")
    void startup_mbti_test() throws Exception {
        // given
        when(mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(eq(RegisterType.SPEND), any(), any()))
                .thenReturn(List.of(
                        new MBTIEmotionAmountSumAndCountDto(MBTIFactor.I, Emotion.PROUD, 500L, 5L)
                ));
        when(mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(eq(RegisterType.SAVE), any(), any()))
                .thenReturn(List.of(
                        new MBTIEmotionAmountSumAndCountDto(MBTIFactor.I, Emotion.PROUD, 1000L, 10L)
                ));

        // when
        targetService.initStatisticsCache();

        // then
        Map<String, Object> mbtiEmotionAmountSums = cacheHashRepository.getHashEntries(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SPEND));
        assertThat(mbtiEmotionAmountSums.get("I::PROUD")).isEqualTo(500L);

        Map<String, Object> mbtiEmotionAmountCounts = cacheHashRepository.getHashEntries(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SPEND));
        assertThat(mbtiEmotionAmountCounts.get("I::PROUD")).isEqualTo(5L);

        mbtiEmotionAmountSums = cacheHashRepository.getHashEntries(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType.SAVE));
        assertThat(mbtiEmotionAmountSums.get("I::PROUD")).isEqualTo(1000L);

        mbtiEmotionAmountCounts = cacheHashRepository.getHashEntries(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType.SAVE));
        assertThat(mbtiEmotionAmountCounts.get("I::PROUD")).isEqualTo(10L);


    }

    @Test
    @DisplayName("레포지토리로부터 통계 데이터를 받아 캐시에 제로 패딩을 한 후 저장한다.")
    void startup_test_zero_padding() throws Exception {
        // given
        List<GenderEmotionAmountSumAndCountDto> returned = List.of(
                new GenderEmotionAmountSumAndCountDto(Gender.MALE, Emotion.PROUD, 1L, 1L),
                new GenderEmotionAmountSumAndCountDto(Gender.MALE, Emotion.SAD, 2L, 1L),
                new GenderEmotionAmountSumAndCountDto(Gender.FEMALE, Emotion.PROUD, 3L, 1L),
                new GenderEmotionAmountSumAndCountDto(Gender.FEMALE, Emotion.SAD, 4L, 1L)
        );

        when(genderStatisticsRepository.getAmountSumsAndCountsEachGenderAndEmotionBetweenStartDateAndEndDate(eq(RegisterType.SPEND), any() , any()))
                .thenReturn(returned);

        // when
        targetService.initStatisticsCache();

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