package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.MBTIEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.MBTISatisfactionAverageDto;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MBTIStatisticsCacheFallbackService {
    private final MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService;
    private final MBTIStatisticsCacheFetchService mbtiStatisticsCacheFetchService;
    private final MBTIStatisticsCacheWriteService mbtiStatisticsCacheWriteService;
    private final StatisticsPeriodManager statisticsPeriodManager;
    public List<MBTIEmotionAmountAverageDto> getAmountAveragesEachMBTIAndEmotion(RegisterType registerType) {
        try {
            return convertToMBTIEmotionAmountAverageDto(mbtiStatisticsCacheFetchService
                    .getAmountAveragesEachMBTIAndEmotion(registerType));
        } catch(InvalidCacheException e) {
            log.warn("MBTIEmotionAmountAverage Cache fallback occurred. Query Database and Cache will be updated.", e);

            StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            SumAndCountStatisticsData<Long> mbtiEmotionAmountSumAndCountStatisticsData = mbtiStatisticsRepositoryFetchService
                    .getMBTIEmotionAmountCountsAndSums(registerType, startDate, endDate);

            mbtiStatisticsCacheWriteService.putAmountCountsAndSumsByMBTIAndEmotion(mbtiEmotionAmountSumAndCountStatisticsData, registerType);

            return convertToMBTIEmotionAmountAverageDto(
                    mbtiEmotionAmountSumAndCountStatisticsData);
        }
    }

    public List<MBTIDailyAmountSumDto> getAmountSumsEachMBTIAndDay(RegisterType registerType) {
        try {
            return convertToMBTIDailyAmountSumDto(
                    mbtiStatisticsCacheFetchService.getAmountSumsEachMBTIAndDay(registerType));
        } catch(InvalidCacheException e) {
            log.warn("MBTIDailyAmountSum Cache fallback occurred. Query Database and Cache will be updated.", e);

            StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            Map<String, Long> mbtiDailyAmountSums = mbtiStatisticsRepositoryFetchService
                    .getMBTIDateAmountSums(registerType, startDate, endDate);

            mbtiStatisticsCacheWriteService.putAmountSumsByMBTIAndDate(mbtiDailyAmountSums, registerType);

            return convertToMBTIDailyAmountSumDto(mbtiDailyAmountSums);
        }
    }

    public List<MBTISatisfactionAverageDto> getSatisfactionAveragesEachMBTI(RegisterType registerType) {
        try {
            return convertToMBTISatisfactionAverageDto(
                    mbtiStatisticsCacheFetchService.getSatisfactionAveragesEachMBTI(registerType));
        } catch(InvalidCacheException e) {
            log.warn("mbtiSatisfactionAverage Cache fallback occurred. Query Database and Cache will be updated.", e);

            StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            SumAndCountStatisticsData<Double> mbtiSatisfactionSumAndCountStatisticsData = mbtiStatisticsRepositoryFetchService
                    .getMBTISatisfactionCountsAndSums(registerType, startDate, endDate);

            mbtiStatisticsCacheWriteService.putSatisfactionCountsAndSumsByMBTI(mbtiSatisfactionSumAndCountStatisticsData, registerType);

            return convertToMBTISatisfactionAverageDto(
                    mbtiSatisfactionSumAndCountStatisticsData);
        }
    }
}
