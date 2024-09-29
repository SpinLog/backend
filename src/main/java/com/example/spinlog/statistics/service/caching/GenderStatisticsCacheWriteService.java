package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheWriteService {
    private final HashCacheService hashCacheService;

    // todo test

    public void putAmountCountsAndSumsByGenderAndEmotion(CountsAndSums amountCountsAndSums, RegisterType registerType) {
        hashCacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(registerType), amountCountsAndSums.countsMap());
        hashCacheService.putAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(registerType), amountCountsAndSums.sumsMap());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        hashCacheService.putAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(CountsAndSums satisfactionCountsAndSums, RegisterType registerType) {
        hashCacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(registerType), satisfactionCountsAndSums.countsMap());
        hashCacheService.putAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(registerType), satisfactionCountsAndSums.sumsMap());
    }
}