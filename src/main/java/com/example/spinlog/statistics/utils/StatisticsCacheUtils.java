package com.example.spinlog.statistics.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderDataDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.user.entity.Gender;
import com.mysql.cj.log.Log;
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
    // todo mbti 캐싱 작업 후 삭제
    public static final int PERIOD_CRITERIA = 30;

    public static Map<String, Long> toGenderEmotionMap(List<GenderEmotionAmountAverageDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));
    }

    public static Map<String, Long> toGenderDateMap(List<GenderDailyAmountSumDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getLocalDate(),
                        GenderDailyAmountSumDto::getAmountSum));
    }

    public static <T extends Number> Map<String, T> toGenderMap(List<GenderDataDto<T>> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender().toString(),
                        GenderDataDto::getValue));
    }

    public static Map<String, Long> toReverseGenderEmotionMap(List<GenderEmotionAmountAverageDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> ((dto.getGender()== MALE)?FEMALE:MALE) + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));
    }

    public static Map<String, Long> toReverseGenderDateMap(List<GenderDailyAmountSumDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> ((dto.getGender()== MALE)?FEMALE:MALE) + "::" + dto.getLocalDate(),
                        GenderDailyAmountSumDto::getAmountSum));
    }

    public static <T extends Number> Map<String, T> toReverseGenderMap(List<GenderDataDto<T>> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> ((dto.getGender()== MALE)?FEMALE:MALE).name(),
                        GenderDataDto::getValue));
    }

    public static List<GenderEmotionAmountAverageDto> convertToGenderEmotionAmountAverageDto(SumAndCountStatisticsData<Long> sumAndCountStatisticsData) {
        Map<String, Long> sumsMap = sumAndCountStatisticsData.sumData();
        Map<String, Long> countsMap = sumAndCountStatisticsData.countData();
        verifyCacheSumsAndCountsMap(sumsMap, countsMap);

        Map<String, Long> genderEmotionAmountAverage = new HashMap<>();
        sumsMap.forEach((k, sum) -> {
            if(sum == 0) {
                genderEmotionAmountAverage.put(k, 0L);
                return;
            }
            long count = countsMap.get(k);
            if(count == 0) {
                throw new InvalidCacheException("sum is not zero, but count is zero, countAndSums = " + sumAndCountStatisticsData);
            }
            long average =  sum / count;
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

    public static List<GenderDailyAmountSumDto> convertToGenderDailyAmountSumDto(Map<String, Long> sumsMap) {
        verifyEntries(sumsMap);

        return sumsMap.entrySet().stream()
                .map(e -> {
                    verifyKeyName(e.getKey());
                    String[] key = e.getKey().split("::");
                    LocalDate date = castLocalDate(key[1]);
                    return new GenderDailyAmountSumDto(
                            castGender(key[0]),
                            date,
                            e.getValue());
                }).toList();
    }

    public static List<GenderSatisfactionAverageDto> convertToGenderSatisfactionAverageDto(SumAndCountStatisticsData<Double> sumAndCountStatisticsData) {
        Map<String, Double> sumsMap = sumAndCountStatisticsData.sumData();
        Map<String, Long> countsMap = sumAndCountStatisticsData.countData();
        verifyCacheSumsAndCountsMap(sumsMap, countsMap);

        Map<String, Float> genderSatisfactionAverage = new HashMap<>();
        sumsMap.forEach((k, sum) -> {
            if(sum == 0.0) {
                genderSatisfactionAverage.put(k, 0f);
                return;
            }
            long count = countsMap.get(k);
            if(count == 0) {
                throw new InvalidCacheException("sum is not zero, but count is zero, countAndSums = " + sumAndCountStatisticsData);
            }
            float average = (float)(sum / (double) count);
            genderSatisfactionAverage.put(k, average);
        });

        return genderSatisfactionAverage.entrySet().stream()
                .map(e -> GenderSatisfactionAverageDto.builder()
                        .gender(castGender(e.getKey()))
                        .satisfactionAverage(e.getValue())
                        .build())
                .toList();
    }

    private static <T extends Number> void verifyCacheSumsAndCountsMap(Map<String, T> sumsMap, Map<String, Long> countsMap) {
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
                throw new InvalidCacheException("Cache sum entries and count entries are not matched, sumData = " + sumsMap + ", countData = " + countsMap);
            }
        });
    }

    private static void verifyEntries(Map<String, Long> entries) {
        if(entries == null) {
            throw new InvalidCacheException("Cache entries are null");
        }
    }

    private static void verifyKeyName(String key) {
        String[] strings = key.split("::");
        if(strings.length != 2) {
            throw new InvalidCacheException("Invalid cache key format, key = " + key);
        }
    }

    // todo 작업은 common 작업으로, 에러만 캐시 에러로 변경

    private static LocalDate castLocalDate(String key) {
        try {
            return LocalDate.parse(key);
        } catch (DateTimeParseException e) {
            throw new InvalidCacheException("Invalid date format, value = " + key, e);
        }
    }

    private static Gender castGender(String key) {
        try {
            return valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new InvalidCacheException("Invalid gender format, value = " + key, e);
        }
    }

    private static Emotion castEmotion(String key) {
        try {
            return Emotion.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new InvalidCacheException("Invalid emotion format, value = " + key, e);
        }
    }
}
