package com.example.spinlog.statistics.scheduled.startup;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.service.GenderStatisticsDataAggregationService;
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
import static com.example.spinlog.statistics.service.GenderStatisticsDataAggregationService.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.*;

@Component
@Transactional(readOnly = true) // todo 범위 좁히기
@RequiredArgsConstructor
@Slf4j
class GenderStatisticsStartupService {
    private final CacheService cacheService;
    private final GenderStatisticsDataAggregationService genderStatisticsDataAggregationService;

    @EventListener(ApplicationReadyEvent.class)
    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching to Redis");
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);

        AllStatisticsMap allData = genderStatisticsDataAggregationService.getGenderStatisticsAllData(startDate, endDate);

        // todo 기존 데이터 검증 후 캐싱

        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND),
                allData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SPEND),
                allData.genderEmotionAmountSpendCountsAndSums().countsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SAVE),
                allData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SAVE),
                allData.genderEmotionAmountSaveCountsAndSums().countsMap());

        cacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SPEND),
                allData.genderDailyAmountSpendSums());
        cacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SAVE),
                allData.genderDailyAmountSaveSums());

        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SPEND),
                allData.genderSatisfactionSpendCountsAndSums().sumsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SPEND),
                allData.genderSatisfactionSpendCountsAndSums().countsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SAVE),
                allData.genderSatisfactionSaveCountsAndSums().sumsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SAVE),
                allData.genderSatisfactionSaveCountsAndSums().countsMap());

        log.info("Finish initializing Caching to Redis");
    }
}
