package com.example.spinlog.statistics.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.SatisfactionSumAndCountDto;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.MBTIEmotionAmountAverageDto;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.Period;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.PERIOD_CRITERIA;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticsZeroPaddingUtils {

    public static List<GenderEmotionAmountAverageDto> zeroPaddingGenderEmotionAmountAverageDtoList(List<GenderEmotionAmountAverageDto> dtos) {
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

    public static List<GenderDailyAmountSumDto> zeroPaddingToGenderDailyAmountSumDtoList(List<GenderDailyAmountSumDto> dtos) {
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

    public static List<MBTIEmotionAmountAverageDto> zeroPaddingToMBTIEmotionAmountAverageDtoList(List<MBTIEmotionAmountAverageDto> dtos) {
        Stream<MBTIEmotionAmountAverageDto> zeroStream = Arrays.stream(Emotion.values())
                .flatMap(e ->
                        Arrays.stream(MBTIFactor.values())
                                .map(f -> new MBTIEmotionAmountAverageDto(f, e, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getMbtiFactor() == zeroDto.getMbtiFactor()
                                && dto.getEmotion() == zeroDto.getEmotion()));

        Comparator<MBTIEmotionAmountAverageDto> byMbtiFactorAndEmotion = Comparator
                .comparing(MBTIEmotionAmountAverageDto::getMbtiFactor)
                .thenComparing(MBTIEmotionAmountAverageDto::getEmotion);

        return Stream.concat(dtos.stream(), zeroStream)
                .sorted(byMbtiFactorAndEmotion)
                .toList();
    }

    public static List<MBTIDailyAmountSumDto> zeroPaddingToMBTIDailyAmountSumDtoList(List<MBTIDailyAmountSumDto> dtos) {
        Stream<LocalDate> localDateRanges = IntStream.rangeClosed(1, PERIOD_CRITERIA)
                .mapToObj(i -> LocalDate.now().minusDays(i));
        Stream<MBTIDailyAmountSumDto> zeroStream = localDateRanges
                .flatMap(d ->
                        Arrays.stream(MBTIFactor.values())
                                .map(f -> new MBTIDailyAmountSumDto(f, d, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getMbtiFactor() == zeroDto.getMbtiFactor()
                                && dto.getLocalDate().equals(zeroDto.getLocalDate())));

        Comparator<MBTIDailyAmountSumDto> byMbtiFactorAndLocalDate = Comparator
                .comparing(MBTIDailyAmountSumDto::getMbtiFactor)
                .thenComparing(MBTIDailyAmountSumDto::getLocalDate);

        return Stream.concat(
                        dtos.stream()
                                .filter(d ->
                                        !d.getLocalDate().equals(LocalDate.now())),
                        zeroStream)
                .sorted(byMbtiFactorAndLocalDate)
                .toList();
    }

    public static SumAndCountStatisticsData<Long> zeroPaddingToEmotionAmountCountsAndSums(SumAndCountStatisticsData<Long> sumAndCountStatisticsData, List<String> keys) {
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

    public static Map<String, Long> zeroPaddingToAmountSums(Map<String, Long> sums, List<String> keys) {
        verifyKeys(sums, keys);

        Map<String, Long> sumsMap = new HashMap<>();

        keys.forEach(key -> sumsMap.put(
                    key,
                    sums.getOrDefault(key, 0L)));

        return Collections.unmodifiableMap(sumsMap);
    }

    public static SumAndCountStatisticsData<Double> zeroPaddingToSatisfactionAmountCountsAndSums(SumAndCountStatisticsData<Double> sumAndCountStatisticsData, List<String> keys) {
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

    public static AllStatisticsCacheData zeroPaddingAllGenderStatisticsMap(AllStatisticsCacheData allData, Period period) {
        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(
                        zeroPaddingToEmotionAmountCountsAndSums(allData.emotionAmountSpendSumAndCountStatisticsData(), getGenderEmotionKeys()))
                .emotionAmountSaveSumAndCountStatisticsData(
                        zeroPaddingToEmotionAmountCountsAndSums(allData.emotionAmountSaveSumAndCountStatisticsData(), getGenderEmotionKeys()))
                .dailyAmountSpendSums(
                        zeroPaddingToAmountSums(allData.dailyAmountSpendSums(), getGenderDailyKeys(period)))
                .dailyAmountSaveSums(
                        zeroPaddingToAmountSums(allData.dailyAmountSaveSums(), getGenderDailyKeys(period)))
                .satisfactionSpendSumAndCountStatisticsData(
                        zeroPaddingToSatisfactionAmountCountsAndSums(allData.satisfactionSpendSumAndCountStatisticsData(), getGenderKeys()))
                .satisfactionSaveSumAndCountStatisticsData(
                        zeroPaddingToSatisfactionAmountCountsAndSums(allData.satisfactionSaveSumAndCountStatisticsData(), getGenderKeys()))
                .build();
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

    public static AllStatisticsCacheData zeroPaddingAllMBTIStatisticsMap(AllStatisticsCacheData allData, Period period) {
        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(
                        zeroPaddingToEmotionAmountCountsAndSums(allData.emotionAmountSpendSumAndCountStatisticsData(), getMBTIEmotionKeys()))
                .emotionAmountSaveSumAndCountStatisticsData(
                        zeroPaddingToEmotionAmountCountsAndSums(allData.emotionAmountSaveSumAndCountStatisticsData(), getMBTIEmotionKeys()))
                .dailyAmountSpendSums(
                        zeroPaddingToAmountSums(allData.dailyAmountSpendSums(), getMBTIDailyKeys(period)))
                .dailyAmountSaveSums(
                        zeroPaddingToAmountSums(allData.dailyAmountSaveSums(), getMBTIDailyKeys(period)))
                .satisfactionSpendSumAndCountStatisticsData(
                        zeroPaddingToSatisfactionAmountCountsAndSums(allData.satisfactionSpendSumAndCountStatisticsData(), getMBTIKeys()))
                .satisfactionSaveSumAndCountStatisticsData(
                        zeroPaddingToSatisfactionAmountCountsAndSums(allData.satisfactionSaveSumAndCountStatisticsData(), getMBTIKeys()))
                .build();
    }

    public static List<SatisfactionSumAndCountDto> removeIfCountIsZero(List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts) {
        if(satisfactionSpendSumsAndCounts.size() == 1 && satisfactionSpendSumsAndCounts.get(0).getSatisfactionCount() == 0){
            satisfactionSpendSumsAndCounts = List.of();
        }
        return satisfactionSpendSumsAndCounts;
    }
}
