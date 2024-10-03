package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.caching.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheVerifyScheduledService {
    private final GenderStatisticsCacheFetchService genderStatisticsCacheFetchService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    @Scheduled(cron = "0 0 5 * * *")
    public void updateGenderStatisticsCacheIfCacheMiss() {
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND, period);
        updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SAVE, period);

        updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND, period);
        updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SAVE, period);

        updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND, period);
        updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SAVE, period);
    }

    public void updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType registerType, Period period) {
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();
        CountsAndSums cacheData;
        try{
            cacheData = genderStatisticsCacheFetchService
                    .getAmountAveragesEachGenderAndEmotion(registerType);
        } catch (Exception e) {
            log.warn("RegisterType(" + registerType
                    + ") Error occurred while fetching cache data. Cache will be updated.", e);
            CountsAndSums repositoryData = genderStatisticsRepositoryFetchService
                    .getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);
            genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(repositoryData, registerType);
            return;
        }

        CountsAndSums repositoryData = genderStatisticsRepositoryFetchService
                .getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);
        if (isNotSame(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderEmotionAmountAverage Cache Data and Repository Data are not same. Cache will be updated.");
            genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderEmotionAmountAverage Cache Data and Repository Data are same.");
    }

    public void updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType registerType, Period period) {
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();
        Map<String, Object> cacheData;
        try {
            cacheData = genderStatisticsCacheFetchService
                    .getAmountSumsEachGenderAndDay(registerType);
        } catch (Exception e) {
            log.warn("RegisterType(" + registerType
                    + ") Error occurred while fetching cache data. Cache will be updated.", e);
            Map<String, Object> repositoryData = genderStatisticsRepositoryFetchService
                    .getGenderDateAmountSums(registerType, startDate, endDate);
            genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(repositoryData, registerType);
            return;
        }

        Map<String, Object> repositoryData = genderStatisticsRepositoryFetchService
                .getGenderDateAmountSums(registerType, startDate, endDate);
        if (isNotSame(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderDailyAmountSum Cache Data and Repository Data are not same. Cache will be updated.");
            genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderDailyAmountSum Cache Data and Repository Data are same.");
    }


    public void updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType registerType, Period period) {
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();
        CountsAndSums cacheData;
        try {
            cacheData = genderStatisticsCacheFetchService
                    .getSatisfactionAveragesEachGender(registerType);
        } catch (Exception e) {
            log.warn("RegisterType(" + registerType
                    + ") Error occurred while fetching cache data. Cache will be updated.", e);
            CountsAndSums repositoryData = genderStatisticsRepositoryFetchService
                    .getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);
            genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(repositoryData, registerType);
            return;
        }

        CountsAndSums repositoryData = genderStatisticsRepositoryFetchService
                .getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);
        if (isNotSame(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderSatisfactionAverage Cache Data and Repository Data are not same. Cache will be updated.");
            genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderSatisfactionAverage Cache Data and Repository Data are same.");
    }

    private boolean isNotSame(CountsAndSums cacheData, CountsAndSums repositoryData) {
        return !(cacheData.sumsMap().equals(repositoryData.sumsMap()) &&
                cacheData.countsMap().equals(repositoryData.countsMap()));
    }

    private boolean isNotSame(Map<String, Object> cacheData, Map<String, Object> repositoryData) {
        return !cacheData.equals(repositoryData);
    }
}
