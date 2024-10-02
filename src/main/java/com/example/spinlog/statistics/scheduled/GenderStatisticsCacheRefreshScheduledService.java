package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheRefreshScheduledService {
    private final HashCacheService hashCacheService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    // todo prometheus & grafana로 성공 여부 확인
    // todo 0~4시 사이 캐시 데이터 정합성 문제 확인
    // todo read 문제는 없음, but article write 시에 Race condition 발생 가능성 있음 -> 캐시 업데이트 할때 lock 걸어야 함
    //  -> PERIOD CRITERIA을 별도의 클래스로 관리하여 lock 걸어야 함
    //  -> PERIOD CRITERIA를 별도의 클래스로 관리하면 테스트 코드 작성이 용이해짐 (Clock 사용)
    @Scheduled(cron = "0 0 4 * * *")
    public void refreshGenderStatisticsCache() {
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate todayEndDate = period.endDate();
        LocalDate todayStartDate = todayEndDate.minusDays(1);
        AllStatisticsMap newStatisticsData = genderStatisticsRepositoryFetchService
                .getGenderStatisticsAllData(todayStartDate, todayEndDate);

        LocalDate oldEndDate = period.startDate();
        LocalDate oldStartDate = oldEndDate.minusDays(1);
        AllStatisticsMap expiringStatisticsData = genderStatisticsRepositoryFetchService
                .getGenderStatisticsAllData(oldStartDate, oldEndDate);

        // todo lock
        decrementOldCacheData(expiringStatisticsData);
        incrementNewCacheData(newStatisticsData);
        // todo unlock

        statisticsPeriodManager.updateStatisticsPeriod();
    }

    private void incrementNewCacheData(AllStatisticsMap newStatisticsData) {
        newStatisticsData.genderEmotionAmountSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        newStatisticsData.genderEmotionAmountSpendCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SPEND), k, (long)v);
        });

        newStatisticsData.genderEmotionAmountSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });
        newStatisticsData.genderEmotionAmountSaveCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SAVE), k, (long)v);
        });


        newStatisticsData.genderDailyAmountSpendSums().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        newStatisticsData.genderDailyAmountSaveSums().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });

        newStatisticsData.genderSatisfactionSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SPEND), k, (double)v);
        });
        newStatisticsData.genderSatisfactionSpendCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SPEND), k, (double)v);
        });

        newStatisticsData.genderSatisfactionSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SAVE), k, (double)v);
        });
        newStatisticsData.genderSatisfactionSaveCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.incrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SAVE), k, (double)v);
        });

    }

    private void decrementOldCacheData(AllStatisticsMap expiringStatisticsData) {
        expiringStatisticsData.genderEmotionAmountSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        expiringStatisticsData.genderEmotionAmountSpendCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SPEND), k, (long)v);
        });

        expiringStatisticsData.genderEmotionAmountSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });
        expiringStatisticsData.genderEmotionAmountSaveCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SAVE), k, (long)v);
        });

        expiringStatisticsData.genderDailyAmountSpendSums().forEach((k, v) -> {
            long data = (long) hashCacheService.getDataFromHash(getGenderDailyStatisticsAmountSumKeyName(SPEND), k);
            if(data != (long)v)
                log.warn("Data is not same. key: {}, repository: {}, cache: {}", k, data, v);
            hashCacheService.deleteHashKey(getGenderDailyStatisticsAmountSumKeyName(SPEND), k);
        });
        expiringStatisticsData.genderDailyAmountSaveSums().forEach((k, v) -> {
            long data = (long) hashCacheService.getDataFromHash(getGenderDailyStatisticsAmountSumKeyName(SAVE), k);
            if(data != (long)v)
                log.warn("Data is not same. key: {}, repository: {}, cache: {}", k, data, v);
            hashCacheService.deleteHashKey(getGenderDailyStatisticsAmountSumKeyName(SAVE), k);
        });

        expiringStatisticsData.genderSatisfactionSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SPEND), k, (double)v);
        });
        expiringStatisticsData.genderSatisfactionSpendCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SPEND), k, (double)v);
        });

        expiringStatisticsData.genderSatisfactionSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SAVE), k, (double)v);
        });
        expiringStatisticsData.genderSatisfactionSaveCountsAndSums().countsMap().forEach((k, v) -> {
            hashCacheService.decrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SAVE), k, (double)v);
        });
    }
}
