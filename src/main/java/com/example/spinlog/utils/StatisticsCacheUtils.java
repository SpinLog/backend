package com.example.spinlog.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.spinlog.user.entity.Gender.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsCacheUtils {
    public static final int PERIOD_CRITERIA = 30;

    public static Map<String, Object> toGenderEmotionMap(List<GenderEmotionAmountAverageDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));
    }

    public static Map<String, Object> toGenderDateMap(List<GenderDailyAmountSumDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getLocalDate(),
                        GenderDailyAmountSumDto::getAmountSum));
    }

    public static <T extends Number> Map<String, Object> toGenderMap(List<GenderDataDto<T>> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender().toString(),
                        GenderDataDto::getValue));
    }

    // todo Map to Map reverse, not list
    public static Map<String, Object> toReverseGenderEmotionMap(List<GenderEmotionAmountAverageDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> ((dto.getGender()== MALE)?FEMALE:MALE) + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));
    }

    public static Map<String, Object> toReverseGenderDateMap(List<GenderDailyAmountSumDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> ((dto.getGender()== MALE)?FEMALE:MALE) + "::" + dto.getLocalDate(),
                        GenderDailyAmountSumDto::getAmountSum));
    }

    public static <T extends Number> Map<String, Object> toReverseGenderMap(List<GenderDataDto<T>> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> ((dto.getGender()== MALE)?FEMALE:MALE).name(),
                        GenderDataDto::getValue));
    }

    public static List<GenderEmotionAmountAverageDto> convertToGenderEmotionAmountAverageDto(CountsAndSums countsAndSums) {
        Map<String, Object> sumsMap = countsAndSums.sumsMap();
        Map<String, Object> countsMap = countsAndSums.countsMap();
        verifyCacheSumsAndCountsMap(sumsMap, countsMap);

        Map<String, Long> genderEmotionAmountAverage = new HashMap<>();
        sumsMap.forEach((k, v) -> {
            long amount = castLong(v);
            if(amount == 0) {
                genderEmotionAmountAverage.put(k, 0L);
                return;
            }
            long count = castLong(countsMap.get(k));
            if(count == 0) {
                throw new InvalidCacheException("sum is not zero, but count is zero");
            }
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

    public static List<GenderDailyAmountSumDto> convertToGenderDailyAmountSumDto(Map<String, Object> sumsMap) {
        verifyEntries(sumsMap);

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

    public static List<GenderSatisfactionAverageDto> convertToGenderSatisfactionAverageDto(CountsAndSums countsAndSums) {
        Map<String, Object> sumsMap = countsAndSums.sumsMap();
        Map<String, Object> countsMap = countsAndSums.countsMap();
        verifyCacheSumsAndCountsMap(sumsMap, countsMap);

        Map<String, Float> genderSatisfactionAverage = new HashMap<>();
        sumsMap.forEach((k, v) -> {
            double satisfactionSum = castDouble(v);
            if(satisfactionSum == 0.0) {
                genderSatisfactionAverage.put(k, 0f);
                return;
            }
            long count = castLong(countsMap.get(k));
            if(count == 0) {
                throw new InvalidCacheException("sum is not zero, but count is zero");
            }
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

    private static void verifyCacheSumsAndCountsMap(Map<String, Object> sumsMap, Map<String, Object> countsMap) {
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

    private static void verifyEntries(Map<String, Object> entries) {
        if(entries == null) {
            throw new InvalidCacheException("Cache entries are null");
        }
    }

    private static void verifyKeyName(String key) {
        String[] strings = key.split("::");
        if(strings.length != 2) {
            throw new InvalidCacheException("Invalid cache key format");
        }
    }

    // todo 작업은 common 작업으로, 에러만 캐시 에러로 변경
    private static long castLong(Object o) {
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            throw new InvalidCacheException("Invalid long format", e);
        }
    }

    private static double castDouble(Object o) {
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            throw new InvalidCacheException("Invalid double format", e);
        }
    }

    private static LocalDate castLocalDate(String key) {
        try {
            return LocalDate.parse(key);
        } catch (DateTimeParseException e) {
            throw new InvalidCacheException("Invalid date format", e);
        }
    }

    private static Gender castGender(String key) {
        try {
            return valueOf(key);
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
