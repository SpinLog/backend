package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
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

    public void decrementAllData(AllStatisticsMap statisticsAllData) {
        hashCacheService.decrementAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        hashCacheService.decrementAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        hashCacheService.decrementAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        hashCacheService.decrementAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        hashCacheService.decrementAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        hashCacheService.decrementAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        hashCacheService.decrementAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    public void incrementAllData(AllStatisticsMap statisticsAllData) {
        hashCacheService.incrementAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        hashCacheService.incrementAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        hashCacheService.incrementAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        hashCacheService.incrementAllDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        hashCacheService.incrementAllDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        hashCacheService.incrementAllDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        hashCacheService.incrementAllDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }
}