package com.example.spinlog.statistics.scheduled.startup;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.AllStatisticsMap;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
class GenderStatisticsCacheStartupService {
    private final HashCacheService hashCacheService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    @EventListener(ApplicationReadyEvent.class)
    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching"); // todo put data with zero padding

        statisticsPeriodManager.setStatisticsPeriodImmediatelyAfterSpringBootIsStarted();

        // have to lock
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();

        AllStatisticsMap allData = genderStatisticsRepositoryFetchService.getGenderStatisticsAllData(startDate, endDate);

        hashCacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                allData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        hashCacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SPEND),
                allData.genderEmotionAmountSpendCountsAndSums().countsMap());
        hashCacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SAVE),
                allData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        hashCacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SAVE),
                allData.genderEmotionAmountSaveCountsAndSums().countsMap());

        hashCacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SPEND),
                allData.genderDailyAmountSpendSums());
        hashCacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SAVE),
                allData.genderDailyAmountSaveSums());

        hashCacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SPEND),
                allData.genderSatisfactionSpendCountsAndSums().sumsMap());
        hashCacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SPEND),
                allData.genderSatisfactionSpendCountsAndSums().countsMap());
        hashCacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SAVE),
                allData.genderSatisfactionSaveCountsAndSums().sumsMap());
        hashCacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SAVE),
                allData.genderSatisfactionSaveCountsAndSums().countsMap());

        log.info("Finish initializing Caching");
    }
}
