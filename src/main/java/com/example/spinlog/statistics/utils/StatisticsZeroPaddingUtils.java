package com.example.spinlog.statistics.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumsAndCounts;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.Period;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.PERIOD_CRITERIA;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsZeroPaddingUtils {

    public static List<GenderEmotionAmountAverageDto> addZeroAverageForMissingGenderEmotionPairs(List<GenderEmotionAmountAverageDto> dtos) {
        Stream<GenderEmotionAmountAverageDto> zeroStream = Arrays.stream(Emotion.values())
                .flatMap(e ->
                        Arrays.stream(Gender.values())
                                .filter(g -> !g.equals(Gender.NONE))
                                .map(g -> new GenderEmotionAmountAverageDto(g, e, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getGender() == zeroDto.getGender()
                                && dto.getEmotion() == zeroDto.getEmotion()));

        Comparator<GenderEmotionAmountAverageDto> byGenderAndEmotion = Comparator
                .comparing(GenderEmotionAmountAverageDto::getGender)
                .thenComparing(GenderEmotionAmountAverageDto::getEmotion);

        return Stream.concat(dtos.stream(), zeroStream)
                .sorted(byGenderAndEmotion)
                .toList();
    }

    public static List<GenderDailyAmountSumDto> addZeroAverageForMissingGenderLocalDatePairs(List<GenderDailyAmountSumDto> dtos) {
        Stream<LocalDate> localDateRanges = IntStream.rangeClosed(1, PERIOD_CRITERIA)
                .mapToObj(i -> LocalDate.now().minusDays(i));
        Stream<GenderDailyAmountSumDto> zeroStream = localDateRanges
                .flatMap(d ->
                        Arrays.stream(Gender.values())
                                .filter(g -> !g.equals(Gender.NONE))
                                .map(g -> new GenderDailyAmountSumDto(g, d, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getGender() == zeroDto.getGender()
                                && dto.getLocalDate().equals(zeroDto.getLocalDate())));

        Comparator<GenderDailyAmountSumDto> byGenderAndLocalDate = Comparator
                .comparing(GenderDailyAmountSumDto::getGender)
                .thenComparing(GenderDailyAmountSumDto::getLocalDate);

        return Stream.concat(
                        dtos.stream()
                                .filter(d ->
                                        !d.getLocalDate().equals(LocalDate.now())),
                        zeroStream)
                .sorted(byGenderAndLocalDate)
                .toList();
    }

    public static SumsAndCounts zeroPaddingToGenderEmotionAmountCountsAndSums(SumsAndCounts sumsAndCounts) {
        List<String> keys = getGenderEmotionKeys();
        verifyKeys(sumsAndCounts, keys);

        Map<String, Object> sumsMap = new HashMap<>();
        Map<String, Object> countsMap = new HashMap<>();

        keys.forEach(key -> {
            countsMap.put(
                    key,
                    sumsAndCounts.countsMap()
                            .getOrDefault(key, 0L));
            sumsMap.put(
                    key,
                    sumsAndCounts.sumsMap()
                            .getOrDefault(key, 0L));
        });

        return new SumsAndCounts(
                Collections.unmodifiableMap(sumsMap),
                Collections.unmodifiableMap(countsMap));
    }

    public static Map<String, Object> zeroPaddingToGenderDailyAmountSums(Map<String, Object> sums, Period period) {
        List<String> keys = getGenderDailyKeys(period);

        verifyKeys(sums, keys);
        Map<String, Object> sumsMap = new HashMap<>();

        keys.forEach(key -> {
            sumsMap.put(
                    key,
                    sums.getOrDefault(key, 0L));

        });

        return Collections.unmodifiableMap(sumsMap);
    }

    public static SumsAndCounts zeroPaddingToGenderSatisfactionAmountCountsAndSums(SumsAndCounts sumsAndCounts) {
        List<String> keys = getGenderKeys();
        verifyKeys(sumsAndCounts, keys);

        Map<String, Object> sumsMap = new HashMap<>();
        Map<String, Object> countsMap = new HashMap<>();

        keys.forEach(key -> {
            countsMap.put(
                    key,
                    sumsAndCounts.countsMap()
                            .getOrDefault(key, 0L));
            sumsMap.put(
                    key,
                    sumsAndCounts.sumsMap()
                            .getOrDefault(key, 0.0));
        });

        return new SumsAndCounts(
                Collections.unmodifiableMap(sumsMap),
                Collections.unmodifiableMap(countsMap));
    }

    public static AllGenderStatisticsCacheData zeroPaddingAllStatisticsMap(AllGenderStatisticsCacheData allData, Period period) {
        return AllGenderStatisticsCacheData.builder()
                .genderEmotionAmountSpendSumsAndCounts(
                        zeroPaddingToGenderEmotionAmountCountsAndSums(allData.genderEmotionAmountSpendSumsAndCounts()))
                .genderEmotionAmountSaveSumsAndCounts(
                        zeroPaddingToGenderEmotionAmountCountsAndSums(allData.genderEmotionAmountSaveSumsAndCounts()))
                .genderDailyAmountSpendSums(
                        zeroPaddingToGenderDailyAmountSums(allData.genderDailyAmountSpendSums(), period))
                .genderDailyAmountSaveSums(
                        zeroPaddingToGenderDailyAmountSums(allData.genderDailyAmountSaveSums(), period))
                .genderSatisfactionSpendSumsAndCounts(
                        zeroPaddingToGenderSatisfactionAmountCountsAndSums(allData.genderSatisfactionSpendSumsAndCounts()))
                .genderSatisfactionSaveSumsAndCounts(
                        zeroPaddingToGenderSatisfactionAmountCountsAndSums(allData.genderSatisfactionSaveSumsAndCounts()))
                .build();
    }

    private static List<String> getGenderEmotionKeys() {
        List<String> keys = Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .flatMap(g ->
                        Arrays.stream(Emotion.values())
                                .map(e -> g + "::" + e))
                .toList();
        return keys;
    }

    private static List<String> getGenderDailyKeys(Period period) {
        LocalDate startDate = period.startDate();
        LocalDate endDate = period.endDate();
        List<String> keys = new ArrayList<>();
        List<Gender> genders = Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .toList();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            String dateString = date.toString();
            genders.forEach(g -> keys.add(g + "::" + dateString));
        }
        return keys;
    }

    private static List<String> getGenderKeys() {
        List<String> keys = Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .map(Gender::name)
                .toList();
        return keys;
    }

    private static void verifyKeys(SumsAndCounts sumsAndCounts, List<String> keys) {
        if(!sumsAndCounts.sumsMap().isEmpty()){
            for(var e: sumsAndCounts.sumsMap().keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, countsAndSums: " + sumsAndCounts);
                }
            }
            for(var e: sumsAndCounts.countsMap().keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, countsAndSums: " + sumsAndCounts);
                }
            }
        }
    }

    private static void verifyKeys(Map<String, Object> sums, List<String> keys) {
        if(!sums.isEmpty()){
            for(var e: sums.keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, sums: " + sums);
                }
            }
        }
    }
}
