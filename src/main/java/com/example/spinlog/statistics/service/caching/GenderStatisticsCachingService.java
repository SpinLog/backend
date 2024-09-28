package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Emotion;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCachingService {
    private final CacheService cacheService;

    // todo Last30Days 이름 변경
    public List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountSumKeyName(registerType));

        Map<String, Object> countsMap = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountCountKeyName(registerType));

        verifyCacheSumsAndCountsEntries(sumsMap, countsMap);

        Map<String, Long> genderEmotionAmountAverage = new HashMap<>();
        sumsMap.forEach((k,v) -> {
            long amount = castLong(v);
            long count = castLong(countsMap.get(k));
            long average =  amount / count;
                    genderEmotionAmountAverage.put(k, average);
                });

        return genderEmotionAmountAverage.entrySet().stream()
                .map(e -> {
                    verifyKeyName(e.getKey());
                    // todo combine verify method & use builder pattern
                    String[] key = e.getKey().split("::");
                    return new GenderEmotionAmountAverageDto(
                            castGender(key[0]),
                            castEmotion(key[1]),
                            e.getValue());
                }).toList();
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDayLast30Days(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderDailyStatisticsAmountSumKeyName(registerType));

        verifyCacheEntries(sumsMap);

        return sumsMap.entrySet().stream()
                .map(e -> {
                    verifyKeyName(e.getKey());
                    String[] key = e.getKey().split("::");
                    LocalDate date = castLocalDate(key[1]);
                    return new GenderDailyAmountSumDto(
                            castGender(key[0]),
                            date,
                            castLong(e.getValue()));
                }).toList();
    }

    public List<MemoDto> getAllMemosByGenderLast30Days(RegisterType registerType, Gender gender) {
        return null;
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGenderLast30Days(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheService.getHashEntries(
                getGenderStatisticsSatisfactionSumKeyName(registerType));

        Map<String, Object> countsMap = cacheService.getHashEntries(
                getGenderStatisticsSatisfactionCountKeyName(registerType));

        verifyCacheSumsAndCountsEntries(sumsMap, countsMap);

        Map<String, Float> genderSatisfactionAverage = new HashMap<>();
        sumsMap.forEach((k,v) -> {
            double satisfactionSum = castDouble(v);
            long count = castLong(countsMap.get(k));
            float average = (float)(satisfactionSum / (double) count);
            genderSatisfactionAverage.put(k, average);
        });

        return genderSatisfactionAverage.entrySet().stream()
                .map(e -> GenderSatisfactionAverageDto.builder()
                        .gender(castGender(e.getKey()))
                        .satisfactionAverage(e.getValue())
                        .build())
                .toList();
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

    private void verifyKeyName(String key) {
        String[] strings = key.split("::");
        if(strings.length != 2) {
            throw new InvalidCacheException("Invalid cache key format");
        }
    }

    private long castLong(Object o) {
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            throw new InvalidCacheException("Invalid long format", e);
        }
    }

    private double castDouble(Object o) {
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            throw new InvalidCacheException("Invalid double format", e);
        }
    }

    private LocalDate castLocalDate(String key) {
        try {
            return LocalDate.parse(key);
        } catch (DateTimeParseException e) {
            throw new InvalidCacheException("Invalid date format", e);
        }
    }

    private static Gender castGender(String key) {
        try {
            return Gender.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new InvalidCacheException("Invalid gender format", e);
        }
    }

    private static Emotion castEmotion(String key) {
            try {
                return Emotion.valueOf(key);
            } catch (IllegalArgumentException e) {
                throw new InvalidCacheException("Invalid emotion format", e);
            }
    }
}
