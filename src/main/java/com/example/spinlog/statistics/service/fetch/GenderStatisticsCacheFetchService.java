package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.repository.dto.MemoDto;
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
    private final CacheService cacheService;

    public List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotion(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountSumKeyName(registerType));

        Map<String, Object> countsMap = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountCountKeyName(registerType));

        // todo verify를 여기서 하는게 맞는지
        verifyCacheSumsAndCountsEntries(sumsMap, countsMap);

        return convertToGenderEmotionAmountAverageDto(sumsMap, countsMap);
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderDailyStatisticsAmountSumKeyName(registerType));

        verifyCacheEntries(sumsMap);

        return convertToGenderDailyAmountSumDto(sumsMap);
    }

    public List<MemoDto> getAllMemosByGender(RegisterType registerType, Gender gender) {
        return null;
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGender(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderStatisticsSatisfactionSumKeyName(registerType));

        Map<String, Object> countsMap = cacheService.getHashEntries(
                getGenderStatisticsSatisfactionCountKeyName(registerType));

        verifyCacheSumsAndCountsEntries(sumsMap, countsMap);

        return convertToGenderSatisfactionAverageDto(sumsMap, countsMap);
    }

    private void verifyCacheSumsAndCountsEntries(Map<String, Object> sumsMap, Map<String, Object> countsMap) {
        if(sumsMap == null) {
            throw new InvalidCacheException("Cache sum entries are null");
        }
        if(countsMap == null) {
            throw new InvalidCacheException("Cache count entries are null");
        }

        if(sumsMap.size() != countsMap.size()) {
            throw new InvalidCacheException("Cache sum entries and count entries are not matched");
        }

        sumsMap.forEach((key, value) -> {
            if (!countsMap.containsKey(key)) {
                throw new InvalidCacheException("Cache sum entries and count entries are not matched");
            }
        });
    }

    private void verifyCacheEntries(Map<String, Object> cacheEntries) {
        if(cacheEntries == null) {
            throw new InvalidCacheException("Cache entries are null");
        }
    }
}
