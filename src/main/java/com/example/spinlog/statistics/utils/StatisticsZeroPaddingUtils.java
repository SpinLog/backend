package com.example.spinlog.statistics.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
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

    public static SumAndCountStatisticsData<Long> zeroPaddingToGenderEmotionAmountCountsAndSums(SumAndCountStatisticsData<Long> sumAndCountStatisticsData) {
        List<String> keys = getGenderEmotionKeys();
        verifyKeys(sumAndCountStatisticsData, keys);

        Map<String, Long> sumsMap = new HashMap<>();
        Map<String, Long> countsMap = new HashMap<>();

        keys.forEach(key -> {
            countsMap.put(
                    key,
                    sumAndCountStatisticsData.countData()
                            .getOrDefault(key, 0L));
            sumsMap.put(
                    key,
                    sumAndCountStatisticsData.sumData()
                            .getOrDefault(key, 0L));
        });

        return new SumAndCountStatisticsData<>(
                Collections.unmodifiableMap(sumsMap),
                Collections.unmodifiableMap(countsMap));
    }

    public static Map<String, Long> zeroPaddingToGenderDailyAmountSums(Map<String, Long> sums, Period period) {
        List<String> keys = getGenderDailyKeys(period);

        verifyKeys(sums, keys);
        Map<String, Long> sumsMap = new HashMap<>();

        keys.forEach(key -> sumsMap.put(
                    key,
                    sums.getOrDefault(key, 0L)));

        return Collections.unmodifiableMap(sumsMap);
    }

    public static SumAndCountStatisticsData<Double> zeroPaddingToGenderSatisfactionAmountCountsAndSums(SumAndCountStatisticsData<Double> sumAndCountStatisticsData) {
        List<String> keys = getGenderKeys();
        verifyKeys(sumAndCountStatisticsData, keys);

        Map<String, Double> sumsMap = new HashMap<>();
        Map<String, Long> countsMap = new HashMap<>();

        keys.forEach(key -> {
            countsMap.put(
                    key,
                    sumAndCountStatisticsData.countData()
                            .getOrDefault(key, 0L));
            sumsMap.put(
                    key,
                    sumAndCountStatisticsData.sumData()
                            .getOrDefault(key, 0.0));
        });

        return new SumAndCountStatisticsData<>(
                Collections.unmodifiableMap(sumsMap),
                Collections.unmodifiableMap(countsMap));
    }

    public static AllGenderStatisticsCacheData zeroPaddingAllStatisticsMap(AllGenderStatisticsCacheData allData, Period period) {
        return AllGenderStatisticsCacheData.builder()
                .genderEmotionAmountSpendSumAndCountStatisticsData(
                        zeroPaddingToGenderEmotionAmountCountsAndSums(allData.genderEmotionAmountSpendSumAndCountStatisticsData()))
                .genderEmotionAmountSaveSumAndCountStatisticsData(
                        zeroPaddingToGenderEmotionAmountCountsAndSums(allData.genderEmotionAmountSaveSumAndCountStatisticsData()))
                .genderDailyAmountSpendSums(
                        zeroPaddingToGenderDailyAmountSums(allData.genderDailyAmountSpendSums(), period))
                .genderDailyAmountSaveSums(
                        zeroPaddingToGenderDailyAmountSums(allData.genderDailyAmountSaveSums(), period))
                .genderSatisfactionSpendSumAndCountStatisticsData(
                        zeroPaddingToGenderSatisfactionAmountCountsAndSums(allData.genderSatisfactionSpendSumAndCountStatisticsData()))
                .genderSatisfactionSaveSumAndCountStatisticsData(
                        zeroPaddingToGenderSatisfactionAmountCountsAndSums(allData.genderSatisfactionSaveSumAndCountStatisticsData()))
                .build();
    }

    private static List<String> getGenderEmotionKeys() {
        return Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .flatMap(g ->
                        Arrays.stream(Emotion.values())
                                .map(e -> g + "::" + e))
                .toList();
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
        return Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .map(Gender::name)
                .toList();
    }

    private static <T extends Number> void verifyKeys(SumAndCountStatisticsData<T> sumAndCountStatisticsData, List<String> keys) {
        if(!sumAndCountStatisticsData.sumData().isEmpty()){
            for(var e: sumAndCountStatisticsData.sumData().keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, countsAndSums: " + sumAndCountStatisticsData);
                }
            }
            for(var e: sumAndCountStatisticsData.countData().keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, countsAndSums: " + sumAndCountStatisticsData);
                }
            }
        }
    }

    private static <T extends Number> void verifyKeys(Map<String, T> sums, List<String> keys) {
        if(!sums.isEmpty()){
            for(var e: sums.keySet()){
                if(!keys.contains(e)){
                    throw new IllegalArgumentException("Invalid key, sums: " + sums);
                }
            }
        }
    }
}
