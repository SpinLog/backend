package com.example.spinlog.global.startup;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.utils.StatisticsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.*;

@Component
@Transactional(readOnly = true) // todo 범위 좁히기
@RequiredArgsConstructor
@Slf4j
class GenderStatisticsStartupService {
    private final CacheService cacheService;
    private final GenderStatisticsRepository genderStatisticsRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching to Redis");
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);

        Result genderEmotionAmountSpendResult = getGenderEmotionAmountResult(SPEND, startDate, endDate);
        Result genderEmotionAmountSaveResult = getGenderEmotionAmountResult(SAVE, startDate, endDate);

        Map<String, Object> genderDailyAmountSpendSum = getGenderDateAmountSum(SPEND, startDate, endDate);
        Map<String, Object> genderDailyAmountSaveSum = getGenderDateAmountSum(SAVE, startDate, endDate);

        Result genderSatisfactionSpendResult = getGenderSatisfactionResult(SPEND, startDate, endDate);
        Result genderSatisfactionSaveResult = getGenderSatisfactionResult(SAVE, startDate, endDate);

        // todo 기존 데이터 검증 후 캐싱

        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                genderEmotionAmountSpendResult.sumsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SPEND),
                genderEmotionAmountSpendResult.countsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SAVE),
                genderEmotionAmountSaveResult.sumsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SAVE),
                genderEmotionAmountSaveResult.countsMap());

        cacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SPEND),
                genderDailyAmountSpendSum);
        cacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SAVE),
                genderDailyAmountSaveSum);

        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SPEND),
                genderSatisfactionSpendResult.sumsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SPEND),
                genderSatisfactionSpendResult.countsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SAVE),
                genderSatisfactionSaveResult.sumsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SAVE),
                genderSatisfactionSaveResult.countsMap());

        log.info("Finish initializing Caching to Redis");
    }

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

    private record Result(Map<String, Object> sumsMap, Map<String, Object> countsMap) { }
}
