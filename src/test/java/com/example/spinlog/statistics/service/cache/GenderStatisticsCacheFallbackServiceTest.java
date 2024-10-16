package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheFallbackServiceTest {
    GenderStatisticsCacheFetchService genderStatisticsCacheFetchService =
            mock(GenderStatisticsCacheFetchService.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            mock(GenderStatisticsRepositoryFetchService.class);
    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService =
             mock(GenderStatisticsCacheWriteService.class);

    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());

    GenderStatisticsCacheFallbackService targetService =
            new GenderStatisticsCacheFallbackService(
                    genderStatisticsCacheFetchService,
                    genderStatisticsRepositoryFetchService,
                    genderStatisticsCacheWriteService,
                    statisticsPeriodManager);
    @Nested
    class getAmountAveragesEachGenderAndEmotion {
        @Test
        void CacheFetchService의_반환_값을_그대로_반환한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(new CountsAndSums(Map.of(), Map.of()));
            
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    targetService.getAmountAveragesEachGenderAndEmotion(null);

            // then
            assertThat(dtos).isEqualTo(List.of());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenThrow(new InvalidCacheException());
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(new CountsAndSums(Map.of(), Map.of()));

            // when
            targetService.getAmountAveragesEachGenderAndEmotion(null);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderEmotionAmountCountsAndSums(any(), any(), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터로_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenThrow(new InvalidCacheException());

            CountsAndSums countsAndSums = new CountsAndSums(Map.of("MALE::SAD", 1000L), Map.of("MALE::SAD", 10L));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(countsAndSums);

            // when
            targetService.getAmountAveragesEachGenderAndEmotion(null);

            // then
            verify(genderStatisticsCacheWriteService).putAmountCountsAndSumsByGenderAndEmotion(eq(countsAndSums), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터를_반환한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenThrow(new InvalidCacheException());

            CountsAndSums countsAndSums = new CountsAndSums(Map.of("MALE::SAD", 1000L), Map.of("MALE::SAD", 10L));
            when(genderStatisticsRepositoryFetchService.getGenderEmotionAmountCountsAndSums(any(), any(), any()))
                    .thenReturn(countsAndSums);

            // when
            List<GenderEmotionAmountAverageDto> dtos = targetService.getAmountAveragesEachGenderAndEmotion(null);

            // then
            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getGender()).isEqualTo(Gender.MALE);
            assertThat(dtos.get(0).getEmotion()).isEqualTo(Emotion.SAD);
            assertThat(dtos.get(0).getAmountAverage()).isEqualTo(1000L / 10L);
        }
    }

    @Nested
    class getAmountSumsEachGenderAndDay {
        @Test
        void CacheFetchService의_반환_값을_그대로_반환한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(Map.of());

            // when
            List<GenderDailyAmountSumDto> dtos = targetService.getAmountSumsEachGenderAndDay(null);

            // then
            assertThat(dtos).isEqualTo(List.of());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenThrow(new InvalidCacheException());
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(Map.of());

            // when
            targetService.getAmountSumsEachGenderAndDay(null);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderDateAmountSums(any(), any(), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터로_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenThrow(new InvalidCacheException());

            Map<String, Object> sums = Map.of("MALE::2024-07-19", 1000L);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(sums);

            // when
            targetService.getAmountSumsEachGenderAndDay(null);

            // then
            verify(genderStatisticsCacheWriteService).putAmountSumsByGenderAndDate(eq(sums), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터를_반환한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(any()))
                    .thenThrow(new InvalidCacheException());

            Map<String, Object> sums = Map.of("MALE::2024-07-19", 1000L);
            when(genderStatisticsRepositoryFetchService.getGenderDateAmountSums(any(), any(), any()))
                    .thenReturn(sums);

            // when
            List<GenderDailyAmountSumDto> dtos = targetService.getAmountSumsEachGenderAndDay(null);

            // then
            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getGender()).isEqualTo(Gender.MALE);
            assertThat(dtos.get(0).getLocalDate()).isEqualTo("2024-07-19");
            assertThat(dtos.get(0).getAmountSum()).isEqualTo(1000L);
        }
    }

    @Nested
    class getSatisfactionAveragesEachGender {
        @Test
        void CacheFetchService의_반환_값을_그대로_반환한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(new CountsAndSums(Map.of(), Map.of()));

            // when
            List<GenderSatisfactionAverageDto> dtos = targetService.getSatisfactionAveragesEachGender(null);

            // then
            assertThat(dtos).isEqualTo(List.of());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenThrow(new InvalidCacheException());
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(new CountsAndSums(Map.of(), Map.of()));

            // when
            targetService.getSatisfactionAveragesEachGender(null);

            // then
            verify(genderStatisticsRepositoryFetchService).getGenderSatisfactionCountsAndSums(any(), any(), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터로_CacheWriteService를_호출한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenThrow(new InvalidCacheException());

            CountsAndSums countsAndSums = new CountsAndSums(Map.of("MALE", 5.0), Map.of("MALE", 1L));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(countsAndSums);

            // when
            targetService.getSatisfactionAveragesEachGender(null);

            // then
            verify(genderStatisticsCacheWriteService).putSatisfactionCountsAndSumsByGender(eq(countsAndSums), any());
        }

        @Test
        void CacheFetchService가_InvalidCacheException를_발생시키면_RepositoryFetchService로부터_받은_데이터를_반환한다() throws Exception {
            // given
            when(genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(any()))
                    .thenThrow(new InvalidCacheException());

            CountsAndSums countsAndSums = new CountsAndSums(Map.of("MALE", 5.0), Map.of("MALE", 1L));
            when(genderStatisticsRepositoryFetchService.getGenderSatisfactionCountsAndSums(any(), any(), any()))
                    .thenReturn(countsAndSums);

            // when
            List<GenderSatisfactionAverageDto> dtos = targetService.getSatisfactionAveragesEachGender(null);

            // then
            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getGender()).isEqualTo(Gender.MALE);
            assertThat(dtos.get(0).getSatisfactionAverage()).isEqualTo(5.0f);
        }
    }
}