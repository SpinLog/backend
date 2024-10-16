package com.example.spinlog.statistics.service.cache.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.cache.scheduled.GenderStatisticsCacheVerifyScheduledService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.Map;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheVerifyScheduledServiceTest {
    GenderStatisticsCacheFetchService genderStatisticsCacheFetchService = mock(GenderStatisticsCacheFetchService.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService = mock(GenderStatisticsRepositoryFetchService.class);
    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService = mock(GenderStatisticsCacheWriteService.class);

    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    GenderStatisticsCacheVerifyScheduledService targetService = new GenderStatisticsCacheVerifyScheduledService(
            genderStatisticsCacheFetchService,
            genderStatisticsRepositoryFetchService,
            genderStatisticsCacheWriteService,
            statisticsPeriodManager);

    @Nested
    class updateGenderEmotionAmountAverageCacheIfCacheMiss {
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenThrow(new RuntimeException());

            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2),
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderEmotionAmountCountsAndSums(any(), any(), any());
            verify(genderStatisticsCacheWriteService).replaceAmountCountsAndSumsByGenderAndEmotion(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            CountsAndSums cacheData = new CountsAndSums(
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2),
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2));
            cacheData = zeroPaddingToGenderEmotionAmountCountsAndSums(cacheData);
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(cacheData);
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("MALE::SAD", 1, "MALE::PROUD", 2),
                            Map.of("MALE::SAD", 1, "MALE::PROUD", 2)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).replaceAmountCountsAndSumsByGenderAndEmotion(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            CountsAndSums cacheData = new CountsAndSums(
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2),
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2));
            cacheData = zeroPaddingToGenderEmotionAmountCountsAndSums(cacheData);
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(cacheData);
            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 2),
                    Map.of("MALE::SAD", 1, "MALE::PROUD", 100));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).replaceAmountCountsAndSumsByGenderAndEmotion(any(), any());
        }
    }

    @Nested
    class updateGenderDailyAmountSumCacheIfCacheMiss {
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenThrow(new RuntimeException());

            Map<String, Object> repositoryData = Map.of("MALE::2024-10-05", 1, "MALE::2024-10-04", 2);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderDateAmountSums(any(), any(), any());
            verify(genderStatisticsCacheWriteService).replaceAmountSumsByGenderAndDate(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            Period period = statisticsPeriodManager.getStatisticsPeriod();

            Map<String, Object> cacheData = Map.of("MALE::2024-10-05", 1, "MALE::2024-10-04", 2);
            cacheData = zeroPaddingToGenderDailyAmountSums(cacheData, period);
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(cacheData);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(
                            Map.of("MALE::2024-10-05", 1, "MALE::2024-10-04", 2));

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).replaceAmountSumsByGenderAndDate(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given

            Period period = statisticsPeriodManager.getStatisticsPeriod();
            Map<String, Object> cacheData = Map.of("MALE::2024-10-05", 1, "MALE::2024-10-04", 2);
            cacheData = zeroPaddingToGenderDailyAmountSums(cacheData, period);
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(cacheData);
            Map<String, Object> repositoryData = Map.of("MALE::2024-10-05", 1, "MALE::2024-10-04", 200);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).replaceAmountSumsByGenderAndDate(any(), any());
        }
    }

    @Nested
    class updateGenderSatisfactionAverageCacheIfCacheMiss {
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenThrow(new RuntimeException());
            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("MALE", 1.0, "FEMALE", 2.0),
                    Map.of("MALE", 1, "FEMALE", 2));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderSatisfactionCountsAndSums(any(), any(), any());
            verify(genderStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByGender(any(), any());
        }
        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1, "FEMALE", 2)));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1, "FEMALE", 2)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).replaceSatisfactionCountsAndSumsByGender(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1, "FEMALE", 2)));
            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("MALE", 1.0, "FEMALE", 2.0),
                    Map.of("MALE", 1, "FEMALE", 100));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByGender(any(), any());
        }
        
        @Test
        void 캐시와_레포지토리로부터_받은_sumsMap들이_근사하게_같지_않다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1, "FEMALE", 2)));
            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("MALE", 1.0, "FEMALE", 4.0),
                    Map.of("MALE", 1, "FEMALE", 2));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByGender(any(), any());
        }
    }
}