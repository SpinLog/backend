package com.example.spinlog.statistics.scheduled.startup;

import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils;
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
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
class GenderStatisticsCacheStartupService {
    private final CacheHashRepository cacheHashRepository;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    @EventListener(ApplicationReadyEvent.class)
    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching");

        statisticsPeriodManager.setStatisticsPeriodImmediatelyAfterSpringBootIsStarted();

        // have to lock
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();

        AllStatisticsMap allData = genderStatisticsRepositoryFetchService.getGenderStatisticsAllData(startDate, endDate);
        allData = StatisticsZeroPaddingUtils.zeroPaddingAllStatisticsMap(allData, period);

        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                allData.genderEmotionAmountSpendCountsAndSums().countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE),
                allData.genderEmotionAmountSaveCountsAndSums().countsMap());

        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.genderDailyAmountSpendSums());
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.genderDailyAmountSaveSums());

        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND),
                allData.genderSatisfactionSpendCountsAndSums().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND),
                allData.genderSatisfactionSpendCountsAndSums().countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE),
                allData.genderSatisfactionSaveCountsAndSums().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE),
                allData.genderSatisfactionSaveCountsAndSums().countsMap());

        log.info("Finish initializing Caching\ncacheDate = {}\n", allData);
    }
}
