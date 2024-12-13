package com.example.spinlog.statistics.service.fetch;

import static com.example.spinlog.article.entity.RegisterType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.repository.*;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.repository.SpecificUserStatisticsRepository;
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
    SpecificUserStatisticsRepository specificUserStatisticsRepository = mock(SpecificUserStatisticsRepository.class);

    GenderStatisticsRepositoryFetchService targetService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository, specificUserStatisticsRepository);

    @Test
    void getGenderEmotionAmountCountsAndSums_returnsCorrectData() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        RegisterType registerType = SPEND;
        List<GenderEmotionAmountSumAndCountDto> amountSums = List.of(new GenderEmotionAmountSumAndCountDto(Gender.MALE, Emotion.SAD, 100L, 10L));
        when(genderStatisticsRepository.getAmountSumsAndCountsEachGenderAndEmotionBetweenStartDateAndEndDate(registerType, startDate, endDate))
                .thenReturn(amountSums);

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

        List<GenderSatisfactionSumAndCountDto> satisfactionSumsAndCounts = List.of(
                new GenderSatisfactionSumAndCountDto(Gender.MALE, 4.5, 20L));
        when(genderStatisticsRepository.getSatisfactionSumsAndCountsEachGenderBetweenStartDateAndEndDate(registerType, startDate, endDate)).thenReturn(satisfactionSumsAndCounts);

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

        List<EmotionAmountSumAndCountDto> genderEmotionAmountSpendSums = List.of(new EmotionAmountSumAndCountDto(Emotion.SAD, 100L, 10L));
        List<EmotionAmountSumAndCountDto> genderEmotionAmountSaveSums = List.of(new EmotionAmountSumAndCountDto(Emotion.SAD, 200L, 20L));

        List<DailyAmountSumDto> genderDailyAmountSpendSums = List.of(new DailyAmountSumDto(LocalDate.now(), 300L));
        List<DailyAmountSumDto> genderDailyAmountSaveSums = List.of(new DailyAmountSumDto(LocalDate.now(), 400L));

        List<SatisfactionSumAndCountDto> genderSatisfactionSpendSums = List.of(new SatisfactionSumAndCountDto(4.5, 20L));
        List<SatisfactionSumAndCountDto> genderSatisfactionSaveSums = List.of(new SatisfactionSumAndCountDto(3.5, 30L));

        when(specificUserStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderEmotionAmountSpendSums);
        when(specificUserStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderEmotionAmountSaveSums);

        when(specificUserStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderDailyAmountSpendSums);
        when(specificUserStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderDailyAmountSaveSums);

        when(specificUserStatisticsRepository.getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(userId, SPEND, startDate, endDate)).thenReturn(genderSatisfactionSpendSums);
        when(specificUserStatisticsRepository.getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(userId, SAVE, startDate, endDate)).thenReturn(genderSatisfactionSaveSums);

        // when
        AllStatisticsRepositoryData result = targetService.getGenderStatisticsAllDataByUserId(userId, startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.emotionAmountSpendSumsAndCounts()).isEqualTo(genderEmotionAmountSpendSums);
        assertThat(result.emotionAmountSaveSumsAndCounts()).isEqualTo(genderEmotionAmountSaveSums);
        assertThat(result.dailyAmountSpendSums()).isEqualTo(genderDailyAmountSpendSums);
        assertThat(result.dailyAmountSaveSums()).isEqualTo(genderDailyAmountSaveSums);
        assertThat(result.satisfactionSpendSumsAndCounts()).isEqualTo(genderSatisfactionSpendSums);
        assertThat(result.satisfactionSaveSumsAndCounts()).isEqualTo(genderSatisfactionSaveSums);
    }
}