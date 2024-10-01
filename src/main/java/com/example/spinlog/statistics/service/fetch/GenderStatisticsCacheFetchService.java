package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.repository.dto.MemoDto;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheFetchService {
    private final HashCacheService hashCacheService;

    public CountsAndSums getAmountAveragesEachGenderAndEmotion(RegisterType registerType) {
        Map<String, Object> sumsMap = hashCacheService.getHashEntries(
                getGenderEmotionStatisticsAmountSumKeyName(registerType));

        Map<String, Object> countsMap = hashCacheService.getHashEntries(
                getGenderEmotionStatisticsAmountCountKeyName(registerType));

        return new CountsAndSums(sumsMap, countsMap);
    }

    public Map<String, Object> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        Map<String, Object> sumsMap = hashCacheService.getHashEntries(
                getGenderDailyStatisticsAmountSumKeyName(registerType));

        return sumsMap;
    }

    public List<MemoDto> getAllMemosByGender(RegisterType registerType, Gender gender) {
        return null;
    }

    public CountsAndSums getSatisfactionAveragesEachGender(RegisterType registerType) {
        Map<String, Object> sumsMap = hashCacheService.getHashEntries(
                getGenderStatisticsSatisfactionSumKeyName(registerType));

        Map<String, Object> countsMap = hashCacheService.getHashEntries(
                getGenderStatisticsSatisfactionCountKeyName(registerType));

        return new CountsAndSums(sumsMap, countsMap);
    }
}
