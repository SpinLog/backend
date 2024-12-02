package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.GenderEmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.GenderSatisfactionSumAndCountDto;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.AllGenderStatisticsRepositoryData;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderDataDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
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

    public AllGenderStatisticsRepositoryData getGenderStatisticsAllDataByUserId(Long userId, LocalDate startDate, LocalDate endDate) { // todo : rename
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendSums = genderStatisticsRepository
                .getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendCounts = genderStatisticsRepository
                .getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveSums = genderStatisticsRepository
                .getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveCounts = genderStatisticsRepository
                .getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<GenderDailyAmountSumDto> genderDailyAmountSpendSums = genderStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderDailyAmountSumDto> genderDailyAmountSaveSums = genderStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<GenderDataDto<Double>> genderSatisfactionSpendSums = genderStatisticsRepository
                .getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderDataDto<Long>> genderSatisfactionSpendCounts = genderStatisticsRepository
                .getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderDataDto<Double>> genderSatisfactionSaveSums = genderStatisticsRepository
                .getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);
        List<GenderDataDto<Long>> genderSatisfactionSaveCounts = genderStatisticsRepository
                .getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        return AllGenderStatisticsRepositoryData.builder()
                .genderDailyAmountSaveSums(genderDailyAmountSaveSums)
                .genderDailyAmountSpendSums(genderDailyAmountSpendSums)
                .genderEmotionAmountSaveCounts(genderEmotionAmountSaveCounts)
                .genderEmotionAmountSaveSums(genderEmotionAmountSaveSums)
                .genderEmotionAmountSpendCounts(genderEmotionAmountSpendCounts)
                .genderEmotionAmountSpendSums(genderEmotionAmountSpendSums)
                .genderSatisfactionSaveCounts(genderSatisfactionSaveCounts)
                .genderSatisfactionSaveSums(genderSatisfactionSaveSums)
                .genderSatisfactionSpendCounts(genderSatisfactionSpendCounts)
                .genderSatisfactionSpendSums(genderSatisfactionSpendSums)
                .build();

    }
}
