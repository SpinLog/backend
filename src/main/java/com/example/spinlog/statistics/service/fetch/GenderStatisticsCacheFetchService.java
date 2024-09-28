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

        return convertToGenderEmotionAmountAverageDto(sumsMap, countsMap);
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderDailyStatisticsAmountSumKeyName(registerType));

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

        return convertToGenderSatisfactionAverageDto(sumsMap, countsMap);
    }
}
