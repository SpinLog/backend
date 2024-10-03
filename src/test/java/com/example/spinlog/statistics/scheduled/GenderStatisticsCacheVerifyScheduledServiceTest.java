package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.caching.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.Map;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static org.junit.jupiter.api.Assertions.*;
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
                    Map.of("key1", 1, "key2", 2),
                    Map.of("key1", 1, "key2", 100));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderEmotionAmountCountsAndSums(any(), any(), any());
            verify(genderStatisticsCacheWriteService).putAmountCountsAndSumsByGenderAndEmotion(eq(repositoryData), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("key1", 1, "key2", 2),
                            Map.of("key1", 1, "key2", 2)));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("key1", 1, "key2", 2),
                            Map.of("key1", 1, "key2", 2)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).putAmountCountsAndSumsByGenderAndEmotion(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("key1", 1, "key2", 2),
                            Map.of("key1", 1, "key2", 2)));
            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("key1", 1, "key2", 2),
                    Map.of("key1", 1, "key2", 100));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).putAmountCountsAndSumsByGenderAndEmotion(eq(repositoryData), any());
        }
    }

    @Nested
    class updateGenderDailyAmountSumCacheIfCacheMiss {
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenThrow(new RuntimeException());

            Map<String, Object> repositoryData = Map.of("key1", 1, "key2", 100);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderDateAmountSums(any(), any(), any());
            verify(genderStatisticsCacheWriteService).putAmountSumsByGenderAndDate(eq(repositoryData), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(
                            Map.of("key1", 1, "key2", 2));
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(
                            Map.of("key1", 1, "key2", 2));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).putAmountSumsByGenderAndDate(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(
                            Map.of("key1", 1, "key2", 2));
            Map<String, Object> repositoryData = Map.of("key1", 1, "key2", 100);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(
                            repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).putAmountSumsByGenderAndDate(eq(repositoryData), any());
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
                    Map.of("key1", 1.0, "key2", 2.0),
                    Map.of("key1", 1, "key2", 100));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderSatisfactionCountsAndSums(any(), any(), any());
            verify(genderStatisticsCacheWriteService).putSatisfactionCountsAndSumsByGender(eq(repositoryData), any());
        }
        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("key1", 1.0, "key2", 2.0),
                            Map.of("key1", 1, "key2", 2)));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("key1", 1.0, "key2", 2.0),
                            Map.of("key1", 1, "key2", 2)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).putSatisfactionCountsAndSumsByGender(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(new CountsAndSums(
                            Map.of("key1", 1.0, "key2", 2.0),
                            Map.of("key1", 1, "key2", 2)));
            CountsAndSums repositoryData = new CountsAndSums(
                    Map.of("key1", 1.0, "key2", 2.0),
                    Map.of("key1", 1, "key2", 100));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).putSatisfactionCountsAndSumsByGender(eq(repositoryData), any());
        }
    }
}