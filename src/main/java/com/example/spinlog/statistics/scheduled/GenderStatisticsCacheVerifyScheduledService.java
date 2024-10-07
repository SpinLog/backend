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
import static com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils.*;

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
        log.info("Start verifying Caching, period: {}", period);
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
        repositoryData = zeroPaddingToGenderEmotionAmountCountsAndSums(repositoryData);

        if (areNotEqual(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderEmotionAmountAverage Cache Data and Repository Data are not same. Cache will be updated.\ncacheDate = {}\nrepositoryData = {}\n",
                    cacheData, repositoryData);
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
        repositoryData = zeroPaddingToGenderDailyAmountSums(repositoryData, period);

        if (areNotEqual(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderDailyAmountSum Cache Data and Repository Data are not same. Cache will be updated.\ncacheDate = {}\nrepositoryData = {}\n",
                    cacheData, repositoryData);
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
        repositoryData = zeroPaddingToGenderSatisfactionAmountCountsAndSums(repositoryData);

        if (areNotApproximatelyEqual(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderSatisfactionAverage Cache Data and Repository Data are not same. Cache will be updated.\ncacheDate = {}\nrepositoryData = {}\n",
                    cacheData, repositoryData);
            genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderSatisfactionAverage Cache Data and Repository Data are same.");
    }

    private boolean areNotEqual(CountsAndSums cacheData, CountsAndSums repositoryData) {
        return !(cacheData.sumsMap().equals(repositoryData.sumsMap()) &&
                cacheData.countsMap().equals(repositoryData.countsMap()));
    }

    private boolean areNotApproximatelyEqual(CountsAndSums cacheData, CountsAndSums repositoryData) {
        double epsilon = 0.0001;
        for(var e: cacheData.sumsMap().entrySet()){
            String k = e.getKey();
            Double v1 = (Double) e.getValue();
            Double v2 = (Double) repositoryData.sumsMap().get(k);
            if (Math.abs(v1 - v2) > epsilon)
                return true;
        }

        return !cacheData.countsMap().equals(repositoryData.countsMap());
    }

    private boolean areNotEqual(Map<String, Object> cacheData, Map<String, Object> repositoryData) {
        return !cacheData.equals(repositoryData);
    }
}
