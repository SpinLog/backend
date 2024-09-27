package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
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
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheScheduledService {
    private final CacheService cacheService;
    private final GenderStatisticsRepository genderStatisticsRepository;

    // todo prometheus & grafana로 성공 여부 확인
    // todo 0~4시 사이 캐시 데이터 정합성 문제 확인
    // todo read 문제는 없음, but article write 시에 Race condition 발생 가능성 있음 -> 캐시 업데이트 할때 lock 걸어야 함
    //  -> PERIOD CRITERIA을 별도의 클래스로 관리하여 lock 걸어야 함
    //  -> PERIOD CRITERIA를 별도의 클래스로 관리하면 테스트 코드 작성이 용이해짐 (Clock 사용)
    @Scheduled(cron = "0 0 4 * * *")
    public void refreshGenderStatisticsCache() {
        LocalDate todayEndDate = LocalDate.now();
        LocalDate todayStartDate = todayEndDate.minusDays(1);
        AllStatisticsResult newStatisticsResult = getAllStatisticsResult(todayStartDate, todayEndDate);
        LocalDate oldEndDate = LocalDate.now().minusDays(PERIOD_CRITERIA);
        LocalDate oldStartDate = oldEndDate.minusDays(1);
        AllStatisticsResult expiringStatisticsResult = getAllStatisticsResult(oldStartDate, oldEndDate);

        // todo lock
        decrementOldCacheData(expiringStatisticsResult);
        incrementNewCacheData(newStatisticsResult);
        // todo unlock
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void verifyGenderStatisticsCache() {
        // todo verify current cache data
    }

    private void incrementNewCacheData(AllStatisticsResult newStatisticsResult) {
        newStatisticsResult.genderEmotionAmountSpendResult().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        newStatisticsResult.genderEmotionAmountSpendResult().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SPEND), k, (long)v);
        });

        newStatisticsResult.genderEmotionAmountSaveResult().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });
        newStatisticsResult.genderEmotionAmountSaveResult().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SAVE), k, (long)v);
        });


        newStatisticsResult.genderDailyAmountSpendSum().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        newStatisticsResult.genderDailyAmountSaveSum().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });

        newStatisticsResult.genderSatisfactionSpendResult().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SPEND), k, (double)v);
        });
        newStatisticsResult.genderSatisfactionSpendResult().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SPEND), k, (double)v);
        });

        newStatisticsResult.genderSatisfactionSaveResult().sumsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SAVE), k, (double)v);
        });
        newStatisticsResult.genderSatisfactionSaveResult().countsMap().forEach((k, v) -> {
            cacheService.incrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SAVE), k, (double)v);
        });

    }

    private void decrementOldCacheData(AllStatisticsResult expiringStatisticsResult) {
        expiringStatisticsResult.genderEmotionAmountSpendResult().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        expiringStatisticsResult.genderEmotionAmountSpendResult().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SPEND), k, (long)v);
        });

        expiringStatisticsResult.genderEmotionAmountSaveResult().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });
        expiringStatisticsResult.genderEmotionAmountSaveResult().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderEmotionStatisticsAmountCountKeyName(SAVE), k, (long)v);
        });

        expiringStatisticsResult.genderDailyAmountSpendSum().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SPEND), k, (long)v);
        });
        expiringStatisticsResult.genderDailyAmountSaveSum().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderDailyStatisticsAmountSumKeyName(SAVE), k, (long)v);
        });

        expiringStatisticsResult.genderSatisfactionSpendResult().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SPEND), k, (double)v);
        });
        expiringStatisticsResult.genderSatisfactionSpendResult().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SPEND), k, (double)v);
        });

        expiringStatisticsResult.genderSatisfactionSaveResult().sumsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionSumKeyName(SAVE), k, (double)v);
        });
        expiringStatisticsResult.genderSatisfactionSaveResult().countsMap().forEach((k, v) -> {
            cacheService.decrementDataInHash(getGenderStatisticsSatisfactionCountKeyName(SAVE), k, (double)v);
        });
    }

    private AllStatisticsResult getAllStatisticsResult(LocalDate startDate, LocalDate endDate) {
        Result genderEmotionAmountSpendResult = getGenderEmotionAmountResult(SPEND, startDate, endDate);
        Result genderEmotionAmountSaveResult = getGenderEmotionAmountResult(SAVE, startDate, endDate);

        Map<String, Object> genderDailyAmountSpendSum = getGenderDateAmountSum(SPEND, startDate, endDate);
        Map<String, Object> genderDailyAmountSaveSum = getGenderDateAmountSum(SAVE, startDate, endDate);

        Result genderSatisfactionSpendResult = getGenderSatisfactionResult(SPEND, startDate, endDate);
        Result genderSatisfactionSaveResult = getGenderSatisfactionResult(SAVE, startDate, endDate);

        return AllStatisticsResult.builder()
                .genderEmotionAmountSpendResult(genderEmotionAmountSpendResult)
                .genderEmotionAmountSaveResult(genderEmotionAmountSaveResult)
                .genderDailyAmountSpendSum(genderDailyAmountSpendSum)
                .genderDailyAmountSaveSum(genderDailyAmountSaveSum)
                .genderSatisfactionSpendResult(genderSatisfactionSpendResult)
                .genderSatisfactionSaveResult(genderSatisfactionSaveResult)
                .build();
    }

    // todo duplicate code 제거
    private Result getGenderEmotionAmountResult(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderEmotionAmountAverageDto> amountSums = genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        List<GenderEmotionAmountAverageDto> amountCounts = genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        Map<String, Object> amountSumsMap = toGenderEmotionMap(amountSums);
        Map<String, Object> amountCountsMap = toGenderEmotionMap(amountCounts);

        return new Result(amountSumsMap, amountCountsMap);
    }

    private Map<String, Object> getGenderDateAmountSum(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderDailyAmountSumDto> amountSums = genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        return toGenderDateMap(amountSums);
    }

    private Result getGenderSatisfactionResult(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderDataDto<Double>> satisfactionSums = genderStatisticsRepository.getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        List<GenderDataDto<Long>> satisfactionCounts = genderStatisticsRepository.getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        Map<String, Object> satisfactionSumsMap = toGenderMap(satisfactionSums);
        Map<String, Object> satisfactionCountsMap = toGenderMap(satisfactionCounts);

        return new Result(satisfactionSumsMap, satisfactionCountsMap);
    }

    // todo duplicate code 제거
    private record Result(Map<String, Object> sumsMap, Map<String, Object> countsMap) { }
    @Builder
    private record AllStatisticsResult(
            Result genderEmotionAmountSpendResult,
            Result genderEmotionAmountSaveResult,
            Map<String, Object> genderDailyAmountSpendSum,
            Map<String, Object> genderDailyAmountSaveSum,
            Result genderSatisfactionSpendResult,
            Result genderSatisfactionSaveResult) { }
}
