package com.example.spinlog.integration.init;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Component
@Transactional(readOnly = true) // todo 범위 좁히기
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheSetupService {
    private final HashCacheService hashCacheService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching");

        StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();
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
