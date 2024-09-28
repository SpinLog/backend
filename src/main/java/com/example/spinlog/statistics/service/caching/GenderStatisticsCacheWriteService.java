package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import com.example.spinlog.utils.CacheKeyNameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheWriteService {
    private final CacheService cacheService;

    // todo test

    public void putAmountCountsAndSumsByGenderAndEmotion(CountsAndSums amountCountsAndSums, RegisterType registerType) {
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(registerType), amountCountsAndSums.countsMap());
        cacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(registerType), amountCountsAndSums.sumsMap());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(CountsAndSums satisfactionCountsAndSums, RegisterType registerType) {
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(registerType), satisfactionCountsAndSums.countsMap());
        cacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(registerType), satisfactionCountsAndSums.sumsMap());
    }
}