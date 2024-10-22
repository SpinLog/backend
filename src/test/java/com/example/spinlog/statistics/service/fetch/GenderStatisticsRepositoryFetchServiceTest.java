package com.example.spinlog.statistics.service.fetch;

import static com.example.spinlog.article.entity.RegisterType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.repository.AllGenderStatisticsRepositoryData;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderDataDto;
import com.example.spinlog.user.entity.Gender;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsRepositoryFetchServiceTest {

    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);

    GenderStatisticsRepositoryFetchService targetService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository);

    @Test
    void getGenderEmotionAmountCountsAndSums_returnsCorrectData() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        RegisterType registerType = SPEND;
        List<GenderEmotionAmountAverageDto> amountSums = List.of(new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 100L));
        List<GenderEmotionAmountAverageDto> amountCounts = List.of(new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 10L));
        when(genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(amountSums);
        when(genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(amountCounts);

        // when
        SumAndCountStatisticsData<Long> result = targetService.getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);

        // then
        assertThat(result.sumData())
                .hasSize(1)
                .containsEntry("MALE::SAD", 100L);
        assertThat(result.countData())
                .hasSize(1)
                .containsEntry("MALE::SAD", 10L);
    }

    @Test
    void getGenderDateAmountSums_returnsCorrectData() {
        // given
        RegisterType registerType = RegisterType.SAVE;
        List<GenderDailyAmountSumDto> amountSums = Collections.singletonList(new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now(), 200L));
        when(genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(eq(registerType), any(), any())).thenReturn(amountSums);

        // when
        Map<String, Long> result = targetService.getGenderDateAmountSums(registerType, null, null);

        // then
        assertThat(result)
                .hasSize(1)
                .containsEntry("MALE::" + LocalDate.now(), 200L);
    }

    @Test
    void getGenderSatisfactionCountsAndSums_returnsCorrectData() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        RegisterType registerType = SPEND;

        List<GenderDataDto<Double>> satisfactionSums = List.of(
                GenderDataDto.<Double>builder()
                        .gender(Gender.MALE).value(4.5).build());
        List<GenderDataDto<Long>> satisfactionCounts = List.of(
                GenderDataDto.<Long>builder()
                        .gender(Gender.MALE).value(20L).build());
        when(genderStatisticsRepository.getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(satisfactionSums);
        when(genderStatisticsRepository.getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(satisfactionCounts);

        // when
        SumAndCountStatisticsData<Double> result = targetService.getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);

        // then
        assertThat(result.sumData())
                .hasSize(1)
                .containsEntry("MALE", 4.5);
        assertThat(result.countData())
                .hasSize(1)
                .containsEntry("MALE", 20L);
    }

    @Test
    void getGenderStatisticsAllDataByUserId_returnsCorrectData() {
        // given
        Long userId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendSums = List.of(new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 100L));
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendCounts = List.of(new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 10L));
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveSums = List.of(new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 200L));
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveCounts = List.of(new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 20L));

        List<GenderDailyAmountSumDto> genderDailyAmountSpendSums = List.of(new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now(), 300L));
        List<GenderDailyAmountSumDto> genderDailyAmountSaveSums = List.of(new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now(), 400L));

        List<GenderDataDto<Double>> genderSatisfactionSpendSums = List.of(new GenderDataDto<>(Gender.MALE, 4.5));
        List<GenderDataDto<Long>> genderSatisfactionSpendCounts = List.of(new GenderDataDto<>(Gender.MALE, 20L));
        List<GenderDataDto<Double>> genderSatisfactionSaveSums = List.of(new GenderDataDto<>(Gender.MALE, 3.5));
        List<GenderDataDto<Long>> genderSatisfactionSaveCounts = List.of(new GenderDataDto<>(Gender.MALE, 30L));

        when(genderStatisticsRepository.getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderEmotionAmountSpendSums);
        when(genderStatisticsRepository.getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderEmotionAmountSpendCounts);
        when(genderStatisticsRepository.getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderEmotionAmountSaveSums);
        when(genderStatisticsRepository.getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderEmotionAmountSaveCounts);

        when(genderStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderDailyAmountSpendSums);
        when(genderStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderDailyAmountSaveSums);

        when(genderStatisticsRepository.getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderSatisfactionSpendSums);
        when(genderStatisticsRepository.getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderSatisfactionSpendCounts);
        when(genderStatisticsRepository.getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderSatisfactionSaveSums);
        when(genderStatisticsRepository.getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderSatisfactionSaveCounts);

        // when
        AllGenderStatisticsRepositoryData result = targetService.getGenderStatisticsAllDataByUserId(userId, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.genderDailyAmountSaveSums()).hasSize(1).contains(genderDailyAmountSaveSums.get(0));
        assertThat(result.genderDailyAmountSpendSums()).hasSize(1).contains(genderDailyAmountSpendSums.get(0));
        assertThat(result.genderEmotionAmountSaveCounts()).hasSize(1).contains(genderEmotionAmountSaveCounts.get(0));
        assertThat(result.genderEmotionAmountSaveSums()).hasSize(1).contains(genderEmotionAmountSaveSums.get(0));
        assertThat(result.genderEmotionAmountSpendCounts()).hasSize(1).contains(genderEmotionAmountSpendCounts.get(0));
        assertThat(result.genderEmotionAmountSpendSums()).hasSize(1).contains(genderEmotionAmountSpendSums.get(0));
        assertThat(result.genderSatisfactionSaveCounts()).hasSize(1).contains(genderSatisfactionSaveCounts.get(0));
        assertThat(result.genderSatisfactionSaveSums()).hasSize(1).contains(genderSatisfactionSaveSums.get(0));
        assertThat(result.genderSatisfactionSpendCounts()).hasSize(1).contains(genderSatisfactionSpendCounts.get(0));
        assertThat(result.genderSatisfactionSpendSums()).hasSize(1).contains(genderSatisfactionSpendSums.get(0));
    }
}