package com.example.spinlog.statistics.service.cache.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.cache.MBTIStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.Map;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsCacheVerifyScheduledServiceTest {
    GenderStatisticsCacheFetchService genderStatisticsCacheFetchService = mock(GenderStatisticsCacheFetchService.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService = mock(GenderStatisticsRepositoryFetchService.class);
    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService = mock(GenderStatisticsCacheWriteService.class);


    MBTIStatisticsCacheFetchService mbtiStatisticsCacheFetchService = mock(MBTIStatisticsCacheFetchService.class);
    MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService = mock(MBTIStatisticsRepositoryFetchService.class);
    MBTIStatisticsCacheWriteService mbtiStatisticsCacheWriteService = mock(MBTIStatisticsCacheWriteService.class);

    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    StatisticsCacheVerifyScheduledService targetService = new StatisticsCacheVerifyScheduledService(
            genderStatisticsCacheFetchService,
            genderStatisticsRepositoryFetchService,
            genderStatisticsCacheWriteService,
            mbtiStatisticsCacheFetchService,
            mbtiStatisticsRepositoryFetchService,
            mbtiStatisticsCacheWriteService,
            statisticsPeriodManager);

    @Nested
    class updateGenderEmotionAmountAverageCacheIfCacheMiss {
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenThrow(new RuntimeException());

            SumAndCountStatisticsData<Long> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L),
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L));
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
            SumAndCountStatisticsData<Long> cacheData = new SumAndCountStatisticsData<>(
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L),
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L));
            cacheData = zeroPaddingToEmotionAmountCountsAndSums(cacheData, getGenderEmotionKeys());
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(cacheData);
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L),
                            Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).replaceAmountCountsAndSumsByGenderAndEmotion(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            SumAndCountStatisticsData<Long> cacheData = new SumAndCountStatisticsData<>(
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L),
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L));
            cacheData = zeroPaddingToEmotionAmountCountsAndSums(cacheData, getGenderEmotionKeys());
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(cacheData);
            SumAndCountStatisticsData<Long> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 2L),
                    Map.of("MALE::SAD", 1L, "MALE::PROUD", 100L));
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

            Map<String, Long> repositoryData = Map.of("MALE::2024-10-05", 1L, "MALE::2024-10-04", 2L);
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

            Map<String, Long> cacheData = Map.of("MALE::2024-10-05", 1L, "MALE::2024-10-04", 2L);
            cacheData = zeroPaddingToAmountSums(cacheData, getGenderDailyKeys(period));
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(cacheData);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(
                            Map.of("MALE::2024-10-05", 1L, "MALE::2024-10-04", 2L));

            // when
            targetService.updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService, never()).replaceAmountSumsByGenderAndDate(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given

            Period period = statisticsPeriodManager.getStatisticsPeriod();
            Map<String, Long> cacheData = Map.of("MALE::2024-10-05", 1L, "MALE::2024-10-04", 2L);
            cacheData = zeroPaddingToAmountSums(cacheData, getGenderDailyKeys(period));
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(cacheData);
            Map<String, Long> repositoryData = Map.of("MALE::2024-10-05", 1L, "MALE::2024-10-04", 200L);
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
            SumAndCountStatisticsData<Double> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("MALE", 1.0, "FEMALE", 2.0),
                    Map.of("MALE", 1L, "FEMALE", 2L));
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
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1L, "FEMALE", 2L)));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1L, "FEMALE", 2L)));

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
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1L, "FEMALE", 2L)));
            SumAndCountStatisticsData<Double> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("MALE", 1.0, "FEMALE", 2.0),
                    Map.of("MALE", 1L, "FEMALE", 100L));
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
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("MALE", 1.0, "FEMALE", 2.0),
                            Map.of("MALE", 1L, "FEMALE", 2L)));
            SumAndCountStatisticsData<Double> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("MALE", 1.0, "FEMALE", 4.0),
                    Map.of("MALE", 1L, "FEMALE", 2L));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(genderStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByGender(any(), any());
        }
    }

    @Nested
    class updateMBTIEmotionAmountAverageCacheIfCacheMiss{
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenThrow(new RuntimeException());

            SumAndCountStatisticsData<Long> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("I::SAD", 1L, "I::PROUD", 2L),
                    Map.of("I::SAD", 1L, "I::PROUD", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTIEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsRepositoryFetchService).getMBTIEmotionAmountCountsAndSums(any(), any(), any());
            verify(mbtiStatisticsCacheWriteService).replaceAmountCountsAndSumsByMBTIAndEmotion(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            SumAndCountStatisticsData<Long> cacheData = new SumAndCountStatisticsData<>(
                    Map.of("I::SAD", 1L, "I::PROUD", 2L),
                    Map.of("I::SAD", 1L, "I::PROUD", 2L));
            cacheData = zeroPaddingToEmotionAmountCountsAndSums(cacheData, getMBTIEmotionKeys());
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenReturn(cacheData);
            when(mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("I::SAD", 1L, "I::PROUD", 2L),
                            Map.of("I::SAD", 1L, "I::PROUD", 2L)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTIEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService, never()).replaceAmountCountsAndSumsByMBTIAndEmotion(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            SumAndCountStatisticsData<Long> cacheData = new SumAndCountStatisticsData<>(
                    Map.of("I::SAD", 1L, "I::PROUD", 2L),
                    Map.of("I::SAD", 1L, "I::PROUD", 2L));
            cacheData = zeroPaddingToEmotionAmountCountsAndSums(cacheData, getMBTIEmotionKeys());
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenReturn(cacheData);
            SumAndCountStatisticsData<Long> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("I::SAD", 1L, "I::PROUD", 2L),
                    Map.of("I::SAD", 1L, "I::PROUD", 2000L));
            when(mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTIEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService).replaceAmountCountsAndSumsByMBTIAndEmotion(any(), any());
        }
    }

    @Nested
    class updateMBTIDailyAmountSumCacheIfCacheMiss{
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenThrow(new RuntimeException());

            Map<String, Long> repositoryData = Map.of("I::2024-10-05", 1L, "I::2024-10-04", 2L);
            when(mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTIDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsRepositoryFetchService).getMBTIDateAmountSums(any(), any(), any());
            verify(mbtiStatisticsCacheWriteService).replaceAmountSumsByMBTIAndDate(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            Period period = statisticsPeriodManager.getStatisticsPeriod();

            Map<String, Long> cacheData = Map.of("I::2024-10-05", 1L, "I::2024-10-04", 2L);
            cacheData = zeroPaddingToAmountSums(cacheData, getMBTIDailyKeys(period));
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenReturn(cacheData);
            when(mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(any(), any(), any()))
                    .thenReturn(
                            Map.of("I::2024-10-05", 1L, "I::2024-10-04", 2L));

            // when
            targetService.updateMBTIDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService, never()).replaceAmountSumsByMBTIAndDate(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given

            Period period = statisticsPeriodManager.getStatisticsPeriod();
            Map<String, Long> cacheData = Map.of("I::2024-10-05", 1L, "I::2024-10-04", 20L);
            cacheData = zeroPaddingToAmountSums(cacheData, getMBTIDailyKeys(period));
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenReturn(cacheData);
            Map<String, Long> repositoryData = Map.of("I::2024-10-05", 1L, "I::2024-10-04", 2L);
            when(mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            // when
            targetService.updateMBTIDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService).replaceAmountSumsByMBTIAndDate(any(), any());
        }
    }

    @Nested
    class updateMBTISatisfactionAverageCacheIfCacheMiss{
        @Test
        void 캐시로부터_데이터를_받을_때_에러가_발생한다면_레포지토리의_데이터를_요청한_다음_캐시를_업데이트한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenThrow(new RuntimeException());
            SumAndCountStatisticsData<Double> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("I", 1.0, "E", 2.0),
                    Map.of("I", 1L, "E", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTISatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsRepositoryFetchService).getMBTISatisfactionCountsAndSums(any(), any(), any());
            verify(mbtiStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByMBTI(any(), any());
        }
        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_같다면_CacheWriteService를_호출하지_않는다() throws Exception {
            // given
            SumAndCountStatisticsData<Double> cacheData = new SumAndCountStatisticsData<>(
                    Map.of("I", 1.0, "E", 2.0),
                    Map.of("I", 1L, "E", 2L));
            cacheData = zeroPaddingToSatisfactionAmountCountsAndSums(cacheData, getMBTIKeys());
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenReturn(cacheData);
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("I", 1.0, "E", 2.0),
                            Map.of("I", 1L, "E", 2L)));

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTISatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService, never()).replaceSatisfactionCountsAndSumsByMBTI(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_데이터가_다르다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("I", 1.0, "E", 2.0),
                            Map.of("I", 1L, "E", 2L)));
            SumAndCountStatisticsData<Double> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("I", 1.0, "E", 2.0),
                    Map.of("I", 1L, "E", 200L));
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTISatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByMBTI(any(), any());
        }

        @Test
        void 캐시와_레포지토리로부터_받은_sumsMap들이_근사하게_같지_않다면_CacheWriteService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenReturn(new SumAndCountStatisticsData<>(
                            Map.of("I", 1.0, "E", 2.0),
                            Map.of("I", 1L, "E", 2L)));
            SumAndCountStatisticsData<Double> repositoryData = new SumAndCountStatisticsData<>(
                    Map.of("I", 1.0, "E", 4.0),
                    Map.of("I", 1L, "E", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(repositoryData);

            Period period = statisticsPeriodManager.getStatisticsPeriod();

            // when
            targetService.updateMBTISatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);

            // then
            verify(mbtiStatisticsCacheWriteService).replaceSatisfactionCountsAndSumsByMBTI(any(), any());
        }
    }
}