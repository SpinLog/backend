package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.caching.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.util.CacheConfiguration;
import com.example.spinlog.util.MockHashCacheService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.PERIOD_CRITERIA;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@Import({CacheConfiguration.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheRefreshScheduledServiceTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    MockHashCacheService cacheService = new MockHashCacheService();
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository);
    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService =
            new GenderStatisticsCacheWriteService(cacheService);
    Clock clock = Clock.systemDefaultZone();
    StatisticsPeriodManager statisticsPeriodManager = spy(new StatisticsPeriodManager(clock));

    GenderStatisticsCacheRefreshScheduledService targetService =
            new GenderStatisticsCacheRefreshScheduledService(
                    cacheService,
                    genderStatisticsRepositoryFetchService,
                    genderStatisticsCacheWriteService,
                    statisticsPeriodManager);

    @AfterEach
    void tearDown() {
        cacheService.clear();
    }
    
    @Test
    void 레포지토리에게_오늘_하루와_30일_전의_모든_통계_데이터를_요청한다() throws Exception {
        // given
        LocalDate todayStartDate = LocalDate.now(clock);
        LocalDate todayEndDate = todayStartDate.plusDays(1);

        LocalDate oldStartDate = LocalDate.now(clock).minusDays(PERIOD_CRITERIA);
        LocalDate oldEndDate = oldStartDate.plusDays(1);

        // when
        targetService.refreshGenderStatisticsCache();
        
        // then
        verifyRequestAllStatisticsDataFromRepository(todayStartDate, todayEndDate);
        verifyRequestAllStatisticsDataFromRepository(oldStartDate, oldEndDate);
        verifyNoMoreInteractions(genderStatisticsRepository);
    }

    @Test
    void 레포지토리로부터_받은_31일_전의_데이터로_캐시의_데이터를_감소시킨다() throws Exception {
        // given
        List<String> keys = getGenderEmotionHashKeyNames();
        keys.forEach(key -> {
            cacheService.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                    key, 0L);
        });

        String targetKey = "MALE::SAD";
        cacheService.incrementDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                targetKey, 1000L);

        LocalDate oldStartDate = LocalDate.now(clock).minusDays(PERIOD_CRITERIA);
        LocalDate oldEndDate = oldStartDate.plusDays(1);
        when(genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                eq(SPEND), eq(oldStartDate), eq(oldEndDate)))
                .thenReturn(List.of(
                        new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 500L)));

        // when
        targetService.refreshGenderStatisticsCache();

        // then
        long data = (long) cacheService.getDataFromHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), targetKey);
        assertThat(data).isEqualTo(1000L - 500L);
    }

    @Test
    void 레포지토리로부터_받은_오늘의_데이터로_캐시의_데이터를_증가시킨다() throws Exception {
        // given
        List<String> keys = getGenderEmotionHashKeyNames();
        keys.forEach(key -> {
            cacheService.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                    key, 0L);
        });

        String targetKey = "MALE::SAD";
        cacheService.incrementDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                targetKey, 1000L);

        LocalDate todayStartDate = LocalDate.now(clock);
        LocalDate todayEndDate = todayStartDate.plusDays(1);
        when(genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                eq(SPEND), eq(todayStartDate), eq(todayEndDate)))
                .thenReturn(List.of(
                        new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 500L)));

        // when
        targetService.refreshGenderStatisticsCache();

        // then
        long data = (long) cacheService.getDataFromHash(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), targetKey);
        assertThat(data).isEqualTo(1000L + 500L);
    }

    @Test
    void 레포지토리로부터_받은_31일_전의_GenderDailyAmountSum_데이터로_캐시_필드를_삭제한다() throws Exception {
        // given
        List<String> keys = getGenderDailyHashKeyNames();
        keys.forEach(key -> {
            cacheService.putDataInHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                    key, 0L);
        });

        LocalDate oldStartDate = LocalDate.now(clock).minusDays(PERIOD_CRITERIA);
        LocalDate oldEndDate = oldStartDate.plusDays(1);
        when(genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                eq(SPEND), eq(oldStartDate), eq(oldEndDate)))
                .thenReturn(List.of(
                        new GenderDailyAmountSumDto(Gender.MALE, oldStartDate, 0L)));

        String targetKey = "MALE::" + oldStartDate;

        // when
        targetService.refreshGenderStatisticsCache();

        // then
        assertThat(cacheService.getDataFromHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), targetKey))
                .isNull();
    }

    @Test
    void StatisticsPeriodManager의_Period를_업데이트한다() throws Exception {
        // when
        targetService.refreshGenderStatisticsCache();
        
        // then
        verify(statisticsPeriodManager).updateStatisticsPeriod();
    }

    private void verifyRequestAllStatisticsDataFromRepository(LocalDate startDate, LocalDate endDate) {
        // any() -> SPEND, SAVE
        verify(genderStatisticsRepository, times(2)).getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                any(), eq(startDate), eq(endDate));
        verify(genderStatisticsRepository, times(2)).getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                any(), eq(startDate), eq(endDate));
        verify(genderStatisticsRepository, times(2)).getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                any(), eq(startDate), eq(endDate));
        verify(genderStatisticsRepository, times(2)).getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
                any(), eq(startDate), eq(endDate));
        verify(genderStatisticsRepository, times(2)).getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
                any(), eq(startDate), eq(endDate));
    }

    private List<String> getGenderDailyHashKeyNames(){
        List<LocalDate> localDateRanges = IntStream.rangeClosed(2, 31)
                .mapToObj(i -> LocalDate.now().minusDays(i))
                .toList();
        Gender[] genders = Gender.values();
        return Arrays.stream(genders)
                .filter(g -> !g.equals(Gender.NONE))
                .flatMap(g -> {
                    return localDateRanges.stream()
                            .map(d -> g + "::" + d);
                }).toList();
    }
}