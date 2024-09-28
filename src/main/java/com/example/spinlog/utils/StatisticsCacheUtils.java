package com.example.spinlog.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static List<GenderEmotionAmountAverageDto> convertToGenderEmotionAmountAverageDto(Map<String, Object> sumsMap, Map<String, Object> countsMap) {
        Map<String, Long> genderEmotionAmountAverage = new HashMap<>();
        sumsMap.forEach((k, v) -> {
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

    public static List<GenderDailyAmountSumDto> convertToGenderDailyAmountSumDto(Map<String, Object> sumsMap) {
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

    public static List<GenderSatisfactionAverageDto> convertToGenderSatisfactionAverageDto(Map<String, Object> sumsMap, Map<String, Object> countsMap) {
        Map<String, Float> genderSatisfactionAverage = new HashMap<>();
        sumsMap.forEach((k, v) -> {
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
