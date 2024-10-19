package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.MBTIEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.MBTISatisfactionAverageDto;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MBTIStatisticsCacheFallbackServiceTest {
    MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService = mock(MBTIStatisticsRepositoryFetchService.class);
    MBTIStatisticsCacheFetchService mbtiStatisticsCacheFetchService = mock(MBTIStatisticsCacheFetchService.class);
    MBTIStatisticsCacheWriteService mbtiStatisticsCacheWriteService = mock(MBTIStatisticsCacheWriteService.class);

    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    MBTIStatisticsCacheFallbackService targetService = new MBTIStatisticsCacheFallbackService(
            mbtiStatisticsRepositoryFetchService,
            mbtiStatisticsCacheFetchService,
            mbtiStatisticsCacheWriteService,
            statisticsPeriodManager
    );

    @Nested
    class getAmountAveragesEachMBTIAndEmotion{
        @Test
        void CacheFetchService의_반환_값을_그대로_반환한다() throws Exception {
            // given
            SumAndCountStatisticsData<Long> value = new SumAndCountStatisticsData<>(
                    Map.of("F::SAD", 1000L), Map.of("F::SAD", 2L));
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenReturn(value);

            // when
            List<MBTIEmotionAmountAverageDto> dtos = targetService.getAmountAveragesEachMBTIAndEmotion(null);

            // then
            MBTIEmotionAmountAverageDto dto = dtos.get(0);
            assertThat(dto.getMbtiFactor()).isEqualTo(MBTIFactor.F);
            assertThat(dto.getEmotion()).isEqualTo(Emotion.SAD);
            assertThat(dto.getAmountAverage()).isEqualTo(1000L / 2L);
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenThrow(InvalidCacheException.class);
            when(mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(new SumAndCountStatisticsData<>(Map.of(), Map.of()));

            // when
            targetService.getAmountAveragesEachMBTIAndEmotion(null);

            // then
            verify(mbtiStatisticsRepositoryFetchService).getMBTIEmotionAmountCountsAndSums(any(), any(), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터로_CacheWriteService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenThrow(InvalidCacheException.class);
            SumAndCountStatisticsData<Long> value = new SumAndCountStatisticsData<>(
                    Map.of("F::SAD", 1000L), Map.of("F::SAD", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(value);

            // when
            targetService.getAmountAveragesEachMBTIAndEmotion(null);

            // then
            verify(mbtiStatisticsCacheWriteService).putAmountCountsAndSumsByMBTIAndEmotion(eq(value), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터를_반환한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountAveragesEachMBTIAndEmotion(any()))
                    .thenThrow(InvalidCacheException.class);
            SumAndCountStatisticsData<Long> value = new SumAndCountStatisticsData<>(
                    Map.of("F::SAD", 1000L), Map.of("F::SAD", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(value);

            // when
            List<MBTIEmotionAmountAverageDto> dtos = targetService.getAmountAveragesEachMBTIAndEmotion(null);

            // then
            MBTIEmotionAmountAverageDto dto = dtos.get(0);
            assertThat(dto.getMbtiFactor()).isEqualTo(MBTIFactor.F);
            assertThat(dto.getEmotion()).isEqualTo(Emotion.SAD);
            assertThat(dto.getAmountAverage()).isEqualTo(1000L / 2L);
        }
    }

    @Nested
    class getAmountSumsEachMBTIAndDay {
        @Test
        void CacheFetchService의_반환_값을_그대로_반환한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenReturn(Map.of("F::2024-10-18", 1000L));

            // when
            List<MBTIDailyAmountSumDto> dtos = targetService.getAmountSumsEachMBTIAndDay(null);

            // then
            MBTIDailyAmountSumDto dto = dtos.get(0);
            assertThat(dto.getMbtiFactor()).isEqualTo(MBTIFactor.F);
            assertThat(dto.getLocalDate().toString()).isEqualTo("2024-10-18");
            assertThat(dto.getAmountSum()).isEqualTo(1000L);
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenThrow(InvalidCacheException.class);
            when(mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(any(), any(), any()))
                    .thenReturn(Map.of());

            // when
            targetService.getAmountSumsEachMBTIAndDay(null);

            // then
            verify(mbtiStatisticsRepositoryFetchService).getMBTIDateAmountSums(any(), any(), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터로_CacheWriteService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenThrow(InvalidCacheException.class);
            Map<String, Long> value = Map.of("F::2024-10-18", 1000L);
            when(mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(any(), any(), any()))
                    .thenReturn(value);

            // when
            targetService.getAmountSumsEachMBTIAndDay(null);

            // then
            verify(mbtiStatisticsCacheWriteService).putAmountSumsByMBTIAndDate(eq(value), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터를_반환한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(any()))
                    .thenThrow(InvalidCacheException.class);
            when(mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(any(), any(), any()))
                    .thenReturn(Map.of("F::2024-10-18", 1000L));

            // when
            List<MBTIDailyAmountSumDto> dtos = targetService.getAmountSumsEachMBTIAndDay(null);

            // then
            MBTIDailyAmountSumDto dto = dtos.get(0);
            assertThat(dto.getMbtiFactor()).isEqualTo(MBTIFactor.F);
            assertThat(dto.getLocalDate().toString()).isEqualTo("2024-10-18");
            assertThat(dto.getAmountSum()).isEqualTo(1000L);
        }
    }

    @Nested
    class getSatisfactionAveragesEachMBTI {
        @Test
        void CacheFetchService의_반환_값을_그대로_반환한다() throws Exception {
            // given
            SumAndCountStatisticsData<Double> value = new SumAndCountStatisticsData<>(
                    Map.of("F", 10.0), Map.of("F", 2L));
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenReturn(value);

            // when
            List<MBTISatisfactionAverageDto> dtos = targetService.getSatisfactionAveragesEachMBTI(null);

            // then
            MBTISatisfactionAverageDto dto = dtos.get(0);
            assertThat(dto.getMbtiFactor()).isEqualTo(MBTIFactor.F);
            assertThat(dto.getSatisfactionAverage()).isEqualTo(10.0f / 2L);
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenThrow(InvalidCacheException.class);
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(new SumAndCountStatisticsData<>(Map.of(), Map.of()));

            // when
            targetService.getSatisfactionAveragesEachMBTI(null);

            // then
            verify(mbtiStatisticsRepositoryFetchService).getMBTISatisfactionCountsAndSums(any(), any(), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터로_CacheWriteService를_호출한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenThrow(InvalidCacheException.class);
            SumAndCountStatisticsData<Double> value = new SumAndCountStatisticsData<>(
                    Map.of("F", 10.0), Map.of("F", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(value);

            // when
            targetService.getSatisfactionAveragesEachMBTI(null);

            // then
            verify(mbtiStatisticsCacheWriteService).putSatisfactionCountsAndSumsByMBTI(eq(value), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터를_반환한다() throws Exception {
            // given
            when(mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(any()))
                    .thenThrow(InvalidCacheException.class);
            SumAndCountStatisticsData<Double> value = new SumAndCountStatisticsData<>(
                    Map.of("F", 10.0), Map.of("F", 2L));
            when(mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(value);

            // when
            List<MBTISatisfactionAverageDto> dtos = targetService.getSatisfactionAveragesEachMBTI(null);

            // then
            MBTISatisfactionAverageDto dto = dtos.get(0);
            assertThat(dto.getMbtiFactor()).isEqualTo(MBTIFactor.F);
            assertThat(dto.getSatisfactionAverage()).isEqualTo(10.0f / 2L);
        }
    }

}