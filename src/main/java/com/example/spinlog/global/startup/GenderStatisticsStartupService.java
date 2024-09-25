package com.example.spinlog.global.startup;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
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

@Component // todo 다른 빈에서 접근 막기
@Transactional(readOnly = true) // todo 범위 좁히기
@RequiredArgsConstructor
@Slf4j
class GenderStatisticsStartupService {
    private final CacheService cacheService;
    private final GenderStatisticsRepository genderStatisticsRepository;
    private final int PERIOD_CRITERIA = 30;

    @EventListener(ApplicationReadyEvent.class)
    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching to Redis");
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
        RegisterType SPEND = RegisterType.SPEND;
        RegisterType SAVE = RegisterType.SAVE;

        Result genderEmotionAmountSpendResult = getGenderEmotionAmountResult(SPEND, startDate, endDate);
        Result genderEmotionAmountSaveResult = getGenderEmotionAmountResult(SAVE, startDate, endDate);

        Map<String, Object> genderDailyAmountSpendSum = getGenderDateAmountSum(SPEND, startDate, endDate);
        Map<String, Object> genderDailyAmountSaveSum = getGenderDateAmountSum(SAVE, startDate, endDate);

        Result genderSatisfactionSpendResult = getGenderSatisfactionResult(SPEND, startDate, endDate);
        Result genderSatisfactionSaveResult = getGenderSatisfactionResult(SAVE, startDate, endDate);

        // todo redis key 별도 클래스로 분리
        cacheService.putAllDataInHash("GenderEmotionStatisticsSum::" + SPEND, genderEmotionAmountSpendResult.sumsMap());
        cacheService.putAllDataInHash("GenderEmotionStatisticsCount::" + SPEND, genderEmotionAmountSpendResult.countsMap());
        cacheService.putAllDataInHash("GenderEmotionStatisticsSum::" + SAVE, genderEmotionAmountSaveResult.sumsMap());
        cacheService.putAllDataInHash("GenderEmotionStatisticsCount::" + SAVE, genderEmotionAmountSaveResult.countsMap());

        cacheService.putAllDataInHash("GenderDailyAmountStatisticsSum::" + SPEND, genderDailyAmountSpendSum);
        cacheService.putAllDataInHash("GenderDailyAmountStatisticsSum::" + SAVE, genderDailyAmountSaveSum);

        cacheService.putAllDataInHash("GenderSatisfactionStatisticsSum::" + SPEND, genderSatisfactionSpendResult.sumsMap());
        cacheService.putAllDataInHash("GenderSatisfactionStatisticsCount::" + SPEND, genderSatisfactionSpendResult.countsMap());
        cacheService.putAllDataInHash("GenderSatisfactionStatisticsSum::" + SAVE, genderSatisfactionSaveResult.sumsMap());
        cacheService.putAllDataInHash("GenderSatisfactionStatisticsCount::" + SAVE, genderSatisfactionSaveResult.countsMap());

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

        Map<String, Object> amountSumsMap = amountSums.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));

        Map<String, Object> amountCountsMap = amountCounts.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));
        return new Result(amountSumsMap, amountCountsMap);
    }

    private Map<String, Object> getGenderDateAmountSum(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        return genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                        registerType,
                        startDate,
                        endDate)
                .stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getLocalDate(),
                        GenderDailyAmountSumDto::getAmountSum));
    }

    private Result getGenderSatisfactionResult(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderDataDto<Double>> amountSums = genderStatisticsRepository.getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        List<GenderDataDto<Long>> amountCounts = genderStatisticsRepository.getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        Map<String, Object> amountSumsMap = amountSums.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender().toString(),
                        GenderDataDto::getValue));

        Map<String, Object> amountCountsMap = amountCounts.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender().toString(),
                        GenderDataDto::getValue));
        return new Result(amountSumsMap, amountCountsMap);
    }

    private record Result(Map<String, Object> sumsMap, Map<String, Object> countsMap) { }
}
