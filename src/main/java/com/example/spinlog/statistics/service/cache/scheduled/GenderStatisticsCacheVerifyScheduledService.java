package com.example.spinlog.statistics.service.cache.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
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
        SumAndCountStatisticsData<Long> cacheData;
        try{
            cacheData = genderStatisticsCacheFetchService
                    .getAmountAveragesEachGenderAndEmotion(registerType);
        } catch (Exception e) {
            log.warn("RegisterType(" + registerType
                    + ") Error occurred while fetching cache data. Cache will be updated.", e);
            SumAndCountStatisticsData<Long> repositoryData = genderStatisticsRepositoryFetchService
                    .getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);
            repositoryData = zeroPaddingToEmotionAmountCountsAndSums(repositoryData, getGenderEmotionKeys());
            genderStatisticsCacheWriteService.replaceAmountCountsAndSumsByGenderAndEmotion(repositoryData, registerType);
            return;
        }

        SumAndCountStatisticsData<Long> repositoryData = genderStatisticsRepositoryFetchService
                .getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);
        repositoryData = zeroPaddingToEmotionAmountCountsAndSums(repositoryData, getGenderEmotionKeys());

        if (areNotEqual(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderEmotionAmountAverage Cache Data and Repository Data are not same. Cache will be updated.\ncacheDate = {}\nrepositoryData = {}\n",
                    cacheData, repositoryData);
            genderStatisticsCacheWriteService.replaceAmountCountsAndSumsByGenderAndEmotion(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderEmotionAmountAverage Cache Data and Repository Data are same.");
    }

    public void updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType registerType, Period period) {
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();
        Map<String, Long> cacheData;
        try {
            cacheData = genderStatisticsCacheFetchService
                    .getAmountSumsEachGenderAndDay(registerType);
        } catch (Exception e) {
            log.warn("RegisterType(" + registerType
                    + ") Error occurred while fetching cache data. Cache will be updated.", e);
            Map<String, Long> repositoryData = genderStatisticsRepositoryFetchService
                    .getGenderDateAmountSums(registerType, startDate, endDate);
            repositoryData = zeroPaddingToGenderDailyAmountSums(repositoryData, getGenderDailyKeys(period));
            genderStatisticsCacheWriteService.replaceAmountSumsByGenderAndDate(repositoryData, registerType);
            return;
        }

        Map<String, Long> repositoryData = genderStatisticsRepositoryFetchService
                .getGenderDateAmountSums(registerType, startDate, endDate);
        repositoryData = zeroPaddingToGenderDailyAmountSums(repositoryData, getGenderDailyKeys(period));

        if (areNotEqual(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderDailyAmountSum Cache Data and Repository Data are not same. Cache will be updated.\ncacheDate = {}\nrepositoryData = {}\n",
                    cacheData, repositoryData);
            genderStatisticsCacheWriteService.replaceAmountSumsByGenderAndDate(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderDailyAmountSum Cache Data and Repository Data are same.");
    }


    public void updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType registerType, Period period) {
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();
        SumAndCountStatisticsData<Double> cacheData;
        try {
            cacheData = genderStatisticsCacheFetchService
                    .getSatisfactionAveragesEachGender(registerType);
        } catch (Exception e) {
            log.warn("RegisterType(" + registerType
                    + ") Error occurred while fetching cache data. Cache will be updated.", e);
            SumAndCountStatisticsData<Double> repositoryData = genderStatisticsRepositoryFetchService
                    .getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);
            repositoryData = zeroPaddingToGenderSatisfactionAmountCountsAndSums(repositoryData, getGenderKeys());
            genderStatisticsCacheWriteService.replaceSatisfactionCountsAndSumsByGender(repositoryData, registerType);
            return;
        }

        SumAndCountStatisticsData<Double> repositoryData = genderStatisticsRepositoryFetchService
                .getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);
        repositoryData = zeroPaddingToGenderSatisfactionAmountCountsAndSums(repositoryData, getGenderKeys());

        if (areNotApproximatelyEqual(cacheData, repositoryData)) {
            log.warn("RegisterType(" + registerType
                    + ") GenderSatisfactionAverage Cache Data and Repository Data are not same. Cache will be updated.\ncacheDate = {}\nrepositoryData = {}\n",
                    cacheData, repositoryData);
            genderStatisticsCacheWriteService.replaceSatisfactionCountsAndSumsByGender(repositoryData, registerType);
        }
        else
            log.info("RegisterType(" + registerType
                    + ") GenderSatisfactionAverage Cache Data and Repository Data are same.");
    }

    private boolean areNotEqual(SumAndCountStatisticsData<Long> cacheData, SumAndCountStatisticsData<Long> repositoryData) {
        return !(cacheData.sumData().equals(repositoryData.sumData()) &&
                cacheData.countData().equals(repositoryData.countData()));
    }

    private boolean areNotApproximatelyEqual(SumAndCountStatisticsData<Double> cacheData, SumAndCountStatisticsData<Double> repositoryData) {
        double epsilon = 0.0001;
        for(var e: cacheData.sumData().entrySet()){
            String k = e.getKey();
            Double v1 = e.getValue();
            Double v2 = repositoryData.sumData().get(k);
            if (Math.abs(v1 - v2) > epsilon)
                return true;
        }

        return !cacheData.countData().equals(repositoryData.countData());
    }

    private boolean areNotEqual(Map<String, Long> cacheData, Map<String, Long> repositoryData) {
        return !cacheData.equals(repositoryData);
    }
}
