package com.example.spinlog.statistics.service.fetch;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.AllMBTIStatisticsRepositoryData;
import com.example.spinlog.article.entity.RegisterType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MBTIStatisticsRepositoryFetchServiceTest {

    MBTIStatisticsRepository mbtiStatisticsRepository = mock(MBTIStatisticsRepository.class);

    MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService =
            new MBTIStatisticsRepositoryFetchService(mbtiStatisticsRepository);

    @Test
    void getMBTIEmotionAmountCountsAndSums_returnsCorrectData() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        RegisterType registerType = SPEND;

        List<MBTIEmotionAmountSumAndCountDto> amountSumsAndCounts = List.of(new MBTIEmotionAmountSumAndCountDto(MBTIFactor.I, Emotion.SAD, 100L, 10L));

        when(mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(amountSumsAndCounts);

        // when
        SumAndCountStatisticsData<Long> result = mbtiStatisticsRepositoryFetchService.getMBTIEmotionAmountCountsAndSums(registerType, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.sumData()).hasSize(1).containsEntry("I::SAD", 100L);
        assertThat(result.countData()).hasSize(1).containsEntry("I::SAD", 10L);
    }

    @Test
    void getMBTIDateAmountSums_returnsCorrectData() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        RegisterType registerType = SAVE;

        List<MBTIDailyAmountSumDto> amountSums = List.of(new MBTIDailyAmountSumDto(MBTIFactor.I, LocalDate.now(), 200L));

        when(mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(amountSums);

        // when
        Map<String, Long> result = mbtiStatisticsRepositoryFetchService.getMBTIDateAmountSums(registerType, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1).containsEntry("I::" + LocalDate.now(), 200L);
    }

    @Test
    void getMBTISatisfactionCountsAndSums_returnsCorrectData() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        RegisterType registerType = SPEND;

        List<MBTISatisfactionSumAndCountDto> satisfactionSumsAndCounts = List.of(new MBTISatisfactionSumAndCountDto(MBTIFactor.I, 4.5, 20L));

        when(mbtiStatisticsRepository.getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(satisfactionSumsAndCounts);

        // when
        SumAndCountStatisticsData<Double> result = mbtiStatisticsRepositoryFetchService.getMBTISatisfactionCountsAndSums(registerType, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.sumData()).hasSize(1).containsEntry("I", 4.5);
        assertThat(result.countData()).hasSize(1).containsEntry("I", 20L);
    }

    @Test
    void getAllMBTIStatisticsRepositoryDataByUserId_returnsCorrectData() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        List<EmotionAmountSumAndCountDto> emotionAmountSpendSumsAndCounts = List.of(new EmotionAmountSumAndCountDto(Emotion.SAD, 100L, 10L));
        List<EmotionAmountSumAndCountDto> emotionAmountSaveSumsAndCounts = List.of(new EmotionAmountSumAndCountDto(Emotion.SAD, 200L, 20L));

        List<DailyAmountSumDto> dailyAmountSpendSums = List.of(new DailyAmountSumDto(LocalDate.now(), 300L));
        List<DailyAmountSumDto> dailyAmountSaveSums = List.of(new DailyAmountSumDto(LocalDate.now(), 400L));

        List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts = List.of(new SatisfactionSumAndCountDto(4.5, 20L));
        List<SatisfactionSumAndCountDto> satisfactionSaveSumsAndCounts = List.of(new SatisfactionSumAndCountDto(3.5, 30L));

        when(mbtiStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(emotionAmountSpendSumsAndCounts);
        when(mbtiStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(emotionAmountSaveSumsAndCounts);

        when(mbtiStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(dailyAmountSpendSums);
        when(mbtiStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(dailyAmountSaveSums);

        when(mbtiStatisticsRepository.getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(satisfactionSpendSumsAndCounts);
        when(mbtiStatisticsRepository.getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(satisfactionSaveSumsAndCounts);

        AllMBTIStatisticsRepositoryData result = mbtiStatisticsRepositoryFetchService.getAllMBTIStatisticsRepositoryDataByUserId(userId, startDate, endDate);


        assertThat(result).isNotNull();
        assertThat(result.emotionAmountSpendSumsAndCounts()).hasSize(1).contains(emotionAmountSpendSumsAndCounts.get(0));
        assertThat(result.emotionAmountSaveSumsAndCounts()).hasSize(1).contains(emotionAmountSaveSumsAndCounts.get(0));
        assertThat(result.dailyAmountSpendSums()).hasSize(1).contains(dailyAmountSpendSums.get(0));
        assertThat(result.dailyAmountSaveSums()).hasSize(1).contains(dailyAmountSaveSums.get(0));
        assertThat(result.satisfactionSpendSumsAndCounts()).hasSize(1).contains(satisfactionSpendSumsAndCounts.get(0));
        assertThat(result.satisfactionSaveSumsAndCounts()).hasSize(1).contains(satisfactionSaveSumsAndCounts.get(0));
    }
}