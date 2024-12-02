package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.AllMBTIStatisticsRepositoryData;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MBTIStatisticsRepositoryFetchService {
    private final MBTIStatisticsRepository mbtiStatisticsRepository;

    public AllStatisticsCacheData getMBTIStatisticsAllData(LocalDate startDate, LocalDate endDate) {
        SumAndCountStatisticsData<Long> mbtiEmotionAmountSpendSumAndCountStatisticsData = getMBTIEmotionAmountCountsAndSums(SPEND, startDate, endDate);
        SumAndCountStatisticsData<Long> mbtiEmotionAmountSaveSumAndCountStatisticsData = getMBTIEmotionAmountCountsAndSums(SAVE, startDate, endDate);

        Map<String, Long> mbtiDailyAmountSpendSums = getMBTIDateAmountSums(SPEND, startDate, endDate);
        Map<String, Long> mbtiDailyAmountSaveSums = getMBTIDateAmountSums(SAVE, startDate, endDate);

        SumAndCountStatisticsData<Double> mbtiSatisfactionSpendSumAndCountStatisticsData = getMBTISatisfactionCountsAndSums(SPEND, startDate, endDate);
        SumAndCountStatisticsData<Double> mbtiSatisfactionSaveSumAndCountStatisticsData = getMBTISatisfactionCountsAndSums(SAVE, startDate, endDate);

        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(mbtiEmotionAmountSpendSumAndCountStatisticsData)
                .emotionAmountSaveSumAndCountStatisticsData(mbtiEmotionAmountSaveSumAndCountStatisticsData)
                .dailyAmountSpendSums(mbtiDailyAmountSpendSums)
                .dailyAmountSaveSums(mbtiDailyAmountSaveSums)
                .satisfactionSpendSumAndCountStatisticsData(mbtiSatisfactionSpendSumAndCountStatisticsData)
                .satisfactionSaveSumAndCountStatisticsData(mbtiSatisfactionSaveSumAndCountStatisticsData)
                .build();
    }

    public SumAndCountStatisticsData<Long> getMBTIEmotionAmountCountsAndSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<MBTIEmotionAmountSumAndCountDto> amountSumsAndCounts = mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        return toMBTIEmotionAmountSumAndCountStatisticsData(amountSumsAndCounts);
    }

    public Map<String, Long> getMBTIDateAmountSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<MBTIDailyAmountSumDto> amountSums = mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        return toMBTIDateMap(amountSums);
    }
    public SumAndCountStatisticsData<Double> getMBTISatisfactionCountsAndSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<MBTISatisfactionSumAndCountDto> satisfactionSumsAndCounts = mbtiStatisticsRepository.getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        return toMBTISatisfactionSumAndCountStatisticsData(satisfactionSumsAndCounts);
    }

    public AllMBTIStatisticsRepositoryData getAllMBTIStatisticsRepositoryDataByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        List<EmotionAmountSumAndCountDto> emotionAmountSpendSumsAndCounts = mbtiStatisticsRepository
                .getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<EmotionAmountSumAndCountDto> emotionAmountSaveSumsAndCounts = mbtiStatisticsRepository
                .getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<DailyAmountSumDto> dailyAmountSpendSums = mbtiStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<DailyAmountSumDto> dailyAmountSaveSums = mbtiStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts = mbtiStatisticsRepository
                .getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<SatisfactionSumAndCountDto> satisfactionSaveSumsAndCounts = mbtiStatisticsRepository
                .getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        return AllMBTIStatisticsRepositoryData.builder()
                .emotionAmountSpendSumsAndCounts(emotionAmountSpendSumsAndCounts)
                .emotionAmountSaveSumsAndCounts(emotionAmountSaveSumsAndCounts)
                .dailyAmountSpendSums(dailyAmountSpendSums)
                .dailyAmountSaveSums(dailyAmountSaveSums)
                .satisfactionSpendSumsAndCounts(satisfactionSpendSumsAndCounts)
                .satisfactionSaveSumsAndCounts(satisfactionSaveSumsAndCounts)
                .build();
    }
}
