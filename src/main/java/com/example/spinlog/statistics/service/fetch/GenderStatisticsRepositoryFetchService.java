package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.*;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.SpecificUserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsRepositoryFetchService {
    private final GenderStatisticsRepository genderStatisticsRepository;
    private final SpecificUserStatisticsRepository specificUserStatisticsRepository;

    public AllStatisticsCacheData getGenderStatisticsAllData(LocalDate startDate, LocalDate endDate) {
        SumAndCountStatisticsData<Long> genderEmotionAmountSpendSumAndCountStatisticsData = getGenderEmotionAmountCountsAndSums(SPEND, startDate, endDate);
        SumAndCountStatisticsData<Long> genderEmotionAmountSaveSumAndCountStatisticsData = getGenderEmotionAmountCountsAndSums(SAVE, startDate, endDate);

        Map<String, Long> genderDailyAmountSpendSums = getGenderDateAmountSums(SPEND, startDate, endDate);
        Map<String, Long> genderDailyAmountSaveSums = getGenderDateAmountSums(SAVE, startDate, endDate);

        SumAndCountStatisticsData<Double> genderSatisfactionSpendSumAndCountStatisticsData = getGenderSatisfactionCountsAndSums(SPEND, startDate, endDate);
        SumAndCountStatisticsData<Double> genderSatisfactionSaveSumAndCountStatisticsData = getGenderSatisfactionCountsAndSums(SAVE, startDate, endDate);

        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(genderEmotionAmountSpendSumAndCountStatisticsData)
                .emotionAmountSaveSumAndCountStatisticsData(genderEmotionAmountSaveSumAndCountStatisticsData)
                .dailyAmountSpendSums(genderDailyAmountSpendSums)
                .dailyAmountSaveSums(genderDailyAmountSaveSums)
                .satisfactionSpendSumAndCountStatisticsData(genderSatisfactionSpendSumAndCountStatisticsData)
                .satisfactionSaveSumAndCountStatisticsData(genderSatisfactionSaveSumAndCountStatisticsData)
                .build();
    }

    public SumAndCountStatisticsData<Long> getGenderEmotionAmountCountsAndSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderEmotionAmountSumAndCountDto> amountSumAndCount = genderStatisticsRepository.getAmountSumsAndCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        return toGenderEmotionAmountSumAndCountStatisticsData(amountSumAndCount);
    }

    public Map<String, Long> getGenderDateAmountSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderDailyAmountSumDto> amountSums = genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        return toGenderDateMap(amountSums);
    }

    public SumAndCountStatisticsData<Double> getGenderSatisfactionCountsAndSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderSatisfactionSumAndCountDto> satisfactionSumsAndCounts = genderStatisticsRepository.getSatisfactionSumsAndCountsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        return toGenderSatisfactionSumAndCountStatisticsData(satisfactionSumsAndCounts);
    }

    public AllStatisticsRepositoryData getGenderStatisticsAllDataByUserId(Long userId, LocalDate startDate, LocalDate endDate) { // todo : rename
        List<EmotionAmountSumAndCountDto> emotionAmountSpendSumsAndCounts = specificUserStatisticsRepository
                .getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<EmotionAmountSumAndCountDto> emotionAmountSaveSumsAndCounts = specificUserStatisticsRepository
                .getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<DailyAmountSumDto> dailyAmountSpendSums = specificUserStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<DailyAmountSumDto> dailyAmountSaveSums = specificUserStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts = specificUserStatisticsRepository
                .getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<SatisfactionSumAndCountDto> satisfactionSaveSumsAndCounts = specificUserStatisticsRepository
                .getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        return AllStatisticsRepositoryData.builder()
                .emotionAmountSpendSumsAndCounts(emotionAmountSpendSumsAndCounts)
                .emotionAmountSaveSumsAndCounts(emotionAmountSaveSumsAndCounts)
                .dailyAmountSpendSums(dailyAmountSpendSums)
                .dailyAmountSaveSums(dailyAmountSaveSums)
                .satisfactionSpendSumsAndCounts(satisfactionSpendSumsAndCounts)
                .satisfactionSaveSumsAndCounts(satisfactionSaveSumsAndCounts)
                .build();
    }
}
