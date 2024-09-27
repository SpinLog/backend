package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.scheduled.GenderStatisticsCacheScheduledService;
import com.example.spinlog.statistics.service.GenderStatisticsDataAggregationService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.util.CacheConfiguration;
import com.example.spinlog.util.MockCacheService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.PERIOD_CRITERIA;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@Import({CacheConfiguration.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheScheduledServiceTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    GenderStatisticsDataAggregationService genderStatisticsDataAggregationService =
            new GenderStatisticsDataAggregationService(genderStatisticsRepository);
    MockCacheService cacheService = new MockCacheService();

    GenderStatisticsCacheScheduledService targetService =
            new GenderStatisticsCacheScheduledService(cacheService, genderStatisticsDataAggregationService);

    @AfterEach
    void tearDown() {
        cacheService.clear();
    }
    
    @Test
    void 레포지토리에게_오늘_하루와_31일_전의_모든_통계_데이터를_요청한다() throws Exception {
        // given
        LocalDate todayEndDate = LocalDate.now();
        LocalDate todayStartDate = todayEndDate.minusDays(1);

        LocalDate oldEndDate = LocalDate.now().minusDays(PERIOD_CRITERIA);
        LocalDate oldStartDate = oldEndDate.minusDays(1);

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
                    getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                    key, 0L);
        });

        String targetKey = "MALE::SAD";
        cacheService.incrementDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                targetKey, 1000L);

        LocalDate oldEndDate = LocalDate.now().minusDays(PERIOD_CRITERIA);
        LocalDate oldStartDate = oldEndDate.minusDays(1);
        when(genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                eq(SPEND), eq(oldStartDate), eq(oldEndDate)))
                .thenReturn(List.of(
                        new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 500L)));

        // when
        targetService.refreshGenderStatisticsCache();

        // then
        long data = (long) cacheService.getDataFromHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), targetKey);
        assertThat(data).isEqualTo(1000L - 500L);
    }

    @Test
    void 레포지토리로부터_받은_오늘의_데이터로_캐시의_데이터를_증가시킨다() throws Exception {
        // given
        List<String> keys = getGenderEmotionHashKeyNames();
        keys.forEach(key -> {
            cacheService.putDataInHash(
                    getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                    key, 0L);
        });

        String targetKey = "MALE::SAD";
        cacheService.incrementDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                targetKey, 1000L);

        LocalDate todayEndDate = LocalDate.now();
        LocalDate todayStartDate = todayEndDate.minusDays(1);
        when(genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                eq(SPEND), eq(todayStartDate), eq(todayEndDate)))
                .thenReturn(List.of(
                        new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 500L)));

        // when
        targetService.refreshGenderStatisticsCache();

        // then
        long data = (long) cacheService.getDataFromHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), targetKey);
        assertThat(data).isEqualTo(1000L + 500L);
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
}