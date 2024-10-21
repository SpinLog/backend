package com.example.spinlog.statistics.service.cache.scheduled.startup;

import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
class StatisticsCacheStartupService {
    private final CacheHashRepository cacheHashRepository;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    @EventListener(ApplicationReadyEvent.class)
    public void initStatisticsCache() {
        log.info("Start initializing Caching");

        statisticsPeriodManager.setStatisticsPeriodImmediatelyAfterSpringBootIsStarted();

        // have to lock
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();

        initStatisticsCache(startDate, endDate, period);
        initMBTIStatisticsCache(startDate, endDate, period);
    }

    private void initMBTIStatisticsCache(LocalDate startDate, LocalDate endDate, Period period) {
        AllStatisticsCacheData allData = mbtiStatisticsRepositoryFetchService.getMBTIStatisticsAllData(startDate, endDate);
        allData = StatisticsZeroPaddingUtils.zeroPaddingAllMBTIStatisticsMap(allData, period);

        cacheHashRepository.putAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                allData.emotionAmountSpendSumAndCountStatisticsData().countData());

        cacheHashRepository.putAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE),
                allData.emotionAmountSaveSumAndCountStatisticsData().countData());

        cacheHashRepository.putAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.dailyAmountSpendSums());
        cacheHashRepository.putAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.dailyAmountSaveSums());

        cacheHashRepository.putAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(SPEND),
                allData.satisfactionSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(SPEND),
                allData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.putAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(SAVE),
                allData.satisfactionSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(SAVE),
                allData.satisfactionSaveSumAndCountStatisticsData().countData());

        log.info("Finish initializing MBTI statistics cache\ncacheDate = {}\n", List.of());
    }

    private void initStatisticsCache(LocalDate startDate, LocalDate endDate, Period period) {
        AllStatisticsCacheData allData = genderStatisticsRepositoryFetchService.getGenderStatisticsAllData(startDate, endDate);
        allData = StatisticsZeroPaddingUtils.zeroPaddingAllGenderStatisticsMap(allData, period);

        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                allData.emotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE),
                allData.emotionAmountSaveSumAndCountStatisticsData().countData());

        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.dailyAmountSpendSums());
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.dailyAmountSaveSums());

        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND),
                allData.satisfactionSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND),
                allData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE),
                allData.satisfactionSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE),
                allData.satisfactionSaveSumAndCountStatisticsData().countData());

        log.info("Finish initializing gender statistics cache\ncacheDate = {}\n", allData);
    }
}
