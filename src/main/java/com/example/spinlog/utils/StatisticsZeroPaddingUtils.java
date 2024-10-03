package com.example.spinlog.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.Period;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsZeroPaddingUtils {

    public static CountsAndSums zeroPaddingToGenderEmotionAmountCountsAndSums(CountsAndSums countsAndSums) {
        List<String> keys = getGenderEmotionKeys();
        verifyKeys(countsAndSums, keys);

        Map<String, Object> sumsMap = new HashMap<>();
        Map<String, Object> countsMap = new HashMap<>();

        keys.forEach(key -> {
            countsMap.put(
                    key,
                    countsAndSums.countsMap()
                            .getOrDefault(key, 0L));
            sumsMap.put(
                    key,
                    countsAndSums.sumsMap()
                            .getOrDefault(key, 0L));
        });

        return new CountsAndSums(sumsMap, countsMap);
    }

    public static Map<String, Object> zeroPaddingToGenderDailyAmountSums(Map<String, Object> sums, Period period) {
        List<String> keys = getGenderDailyKeys(period);

        verifyKeys(sums, keys);
        Map<String, Object> sumsMap = new HashMap<>();

        keys.forEach(key -> {
            if (!sums.containsKey(key)) {
                sums.put(key, 0L);
            }
            sumsMap.put(
                    key,
                    sums.getOrDefault(key, 0L));

        });

        return sumsMap;
    }

    public static CountsAndSums zeroPaddingToGenderSatisfactionAmountCountsAndSums(CountsAndSums countsAndSums) {
        List<String> keys = getGenderKeys();
        verifyKeys(countsAndSums, keys);

        Map<String, Object> sumsMap = new HashMap<>();
        Map<String, Object> countsMap = new HashMap<>();

        keys.forEach(key -> {
            countsMap.put(
                    key,
                    countsAndSums.countsMap()
                            .getOrDefault(key, 0L));
            sumsMap.put(
                    key,
                    countsAndSums.sumsMap()
                            .getOrDefault(key, 0.0));
        });

        return new CountsAndSums(sumsMap, countsMap);
    }

    public static AllStatisticsMap zeroPaddingAllStatisticsMap(AllStatisticsMap allData, Period period) {
        return AllStatisticsMap.builder()
                .genderEmotionAmountSpendCountsAndSums(
                        zeroPaddingToGenderEmotionAmountCountsAndSums(allData.genderEmotionAmountSpendCountsAndSums()))
                .genderEmotionAmountSaveCountsAndSums(
                        zeroPaddingToGenderEmotionAmountCountsAndSums(allData.genderEmotionAmountSaveCountsAndSums()))
                .genderDailyAmountSpendSums(
                        zeroPaddingToGenderDailyAmountSums(allData.genderDailyAmountSpendSums(), period))
                .genderDailyAmountSaveSums(
                        zeroPaddingToGenderDailyAmountSums(allData.genderDailyAmountSaveSums(), period))
                .genderSatisfactionSpendCountsAndSums(
                        zeroPaddingToGenderSatisfactionAmountCountsAndSums(allData.genderSatisfactionSpendCountsAndSums()))
                .genderSatisfactionSaveCountsAndSums(
                        zeroPaddingToGenderSatisfactionAmountCountsAndSums(allData.genderSatisfactionSaveCountsAndSums()))
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

    private static void verifyKeys(CountsAndSums countsAndSums, List<String> keys) {
        if(!countsAndSums.sumsMap().isEmpty()){
            for(var e: countsAndSums.sumsMap().keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, countsAndSums: " + countsAndSums);
                }
            }
            for(var e: countsAndSums.countsMap().keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, countsAndSums: " + countsAndSums);
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
