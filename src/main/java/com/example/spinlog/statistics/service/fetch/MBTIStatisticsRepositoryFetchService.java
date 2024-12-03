package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.AllStatisticsRepositoryData;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import com.example.spinlog.statistics.repository.SpecificUserStatisticsRepository;
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
    private final SpecificUserStatisticsRepository specificUserStatisticsRepository;

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

    public AllStatisticsRepositoryData getAllMBTIStatisticsRepositoryDataByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
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
