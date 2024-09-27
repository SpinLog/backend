package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.service.GenderStatisticsDataAggregationService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.GenderStatisticsDataAggregationService.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheScheduledService {
    private final CacheService cacheService;
    private final GenderStatisticsDataAggregationService genderStatisticsDataAggregationService;

    // todo prometheus & grafana로 성공 여부 확인
    // todo 0~4시 사이 캐시 데이터 정합성 문제 확인
    // todo read 문제는 없음, but article write 시에 Race condition 발생 가능성 있음 -> 캐시 업데이트 할때 lock 걸어야 함
    //  -> PERIOD CRITERIA을 별도의 클래스로 관리하여 lock 걸어야 함
    //  -> PERIOD CRITERIA를 별도의 클래스로 관리하면 테스트 코드 작성이 용이해짐 (Clock 사용)
    @Scheduled(cron = "0 0 4 * * *")
    public void refreshGenderStatisticsCache() {
        LocalDate todayEndDate = LocalDate.now();
        LocalDate todayStartDate = todayEndDate.minusDays(1);
        AllStatisticsMap newStatisticsData = genderStatisticsDataAggregationService
                .getGenderStatisticsAllData(todayStartDate, todayEndDate);

        LocalDate oldEndDate = LocalDate.now().minusDays(PERIOD_CRITERIA);
        LocalDate oldStartDate = oldEndDate.minusDays(1);
        AllStatisticsMap expiringStatisticsData = genderStatisticsDataAggregationService
                .getGenderStatisticsAllData(oldStartDate, oldEndDate);

        // todo lock
        decrementOldCacheData(expiringStatisticsData);
        incrementNewCacheData(newStatisticsData);
        // todo unlock
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void verifyGenderStatisticsCache() {
        // todo verify current cache data
    }

    private void incrementNewCacheData(AllStatisticsMap newStatisticsData) {
        newStatisticsData.genderEmotionAmountSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        newStatisticsData.genderEmotionAmountSpendCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SPEND), k, (long)v);
        });

        newStatisticsData.genderEmotionAmountSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });
        newStatisticsData.genderEmotionAmountSaveCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SAVE), k, (long)v);
        });


        newStatisticsData.genderDailyAmountSpendSums().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        newStatisticsData.genderDailyAmountSaveSums().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });

        newStatisticsData.genderSatisfactionSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SPEND), k, (double)v);
        });
        newStatisticsData.genderSatisfactionSpendCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SPEND), k, (double)v);
        });

        newStatisticsData.genderSatisfactionSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SAVE), k, (double)v);
        });
        newStatisticsData.genderSatisfactionSaveCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SAVE), k, (double)v);
        });

    }

    private void decrementOldCacheData(AllStatisticsMap expiringStatisticsData) {
        expiringStatisticsData.genderEmotionAmountSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        expiringStatisticsData.genderEmotionAmountSpendCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SPEND), k, (long)v);
        });

        expiringStatisticsData.genderEmotionAmountSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });
        expiringStatisticsData.genderEmotionAmountSaveCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SAVE), k, (long)v);
        });

        expiringStatisticsData.genderDailyAmountSpendSums().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        expiringStatisticsData.genderDailyAmountSaveSums().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });

        expiringStatisticsData.genderSatisfactionSpendCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SPEND), k, (double)v);
        });
        expiringStatisticsData.genderSatisfactionSpendCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SPEND), k, (double)v);
        });

        expiringStatisticsData.genderSatisfactionSaveCountsAndSums().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SAVE), k, (double)v);
        });
        expiringStatisticsData.genderSatisfactionSaveCountsAndSums().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SAVE), k, (double)v);
        });
    }
}
