package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.service.caching.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheVerifyScheduledService {
    private final GenderStatisticsCacheFetchService genderStatisticsCacheFetchService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;

    @Scheduled(cron = "0 0 5 * * *")
    public void updateGenderStatisticsCacheIfCacheMiss() {
        updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SPEND);
        updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType.SAVE);

        updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SPEND);
        updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType.SAVE);

        updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SPEND);
        updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType.SAVE);
    }

    public void updateGenderEmotionAmountAverageCacheIfCacheMiss(RegisterType registerType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
        CountsAndSums cacheData = genderStatisticsCacheFetchService
                .getAmountAveragesEachGenderAndEmotion(registerType);
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

    public void updateGenderDailyAmountSumCacheIfCacheMiss(RegisterType registerType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
        Map<String, Object> cacheData = genderStatisticsCacheFetchService
                .getAmountSumsEachGenderAndDay(registerType);
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


    public void updateGenderSatisfactionAverageCacheIfCacheMiss(RegisterType registerType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
        CountsAndSums cacheData = genderStatisticsCacheFetchService
                .getSatisfactionAveragesEachGender(registerType);
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
