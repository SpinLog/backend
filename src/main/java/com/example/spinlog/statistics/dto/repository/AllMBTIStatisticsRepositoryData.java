package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.statistics.dto.DailyAmountSumDto;
import com.example.spinlog.statistics.dto.EmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.SatisfactionSumAndCountDto;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.Builder;

import java.util.List;

import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Builder // todo : rename
public record AllMBTIStatisticsRepositoryData (
    List<EmotionAmountSumAndCountDto> emotionAmountSpendSumsAndCounts,
    List<EmotionAmountSumAndCountDto> emotionAmountSaveSumsAndCounts,
    List<DailyAmountSumDto> dailyAmountSpendSums,
    List<DailyAmountSumDto> dailyAmountSaveSums,
    List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts,
    List<SatisfactionSumAndCountDto> satisfactionSaveSumsAndCounts) {
    public AllStatisticsCacheData toCacheDate(MBTIFactor mbtiFactor){
        return AllStatisticsCacheData.builder()
                .emotionAmountSpendSumAndCountStatisticsData(
                        toAmountSumAndCountStatisticsData(toMBTIEmotionAmountDtoList(emotionAmountSpendSumsAndCounts, mbtiFactor)))
                .emotionAmountSaveSumAndCountStatisticsData(
                        toAmountSumAndCountStatisticsData(toMBTIEmotionAmountDtoList(emotionAmountSaveSumsAndCounts, mbtiFactor)))
                .dailyAmountSpendSums(
                        toMBTIDateMap(toMBTIDailyAmountDtoList(dailyAmountSpendSums, mbtiFactor)))
                .dailyAmountSaveSums(
                        toMBTIDateMap(toMBTIDailyAmountDtoList(dailyAmountSaveSums, mbtiFactor)))
                .satisfactionSpendSumAndCountStatisticsData(
                        toSatisfactionSumAndCountStatisticsData(toMBTISatisfactionDtoList(satisfactionSpendSumsAndCounts, mbtiFactor)))
                .satisfactionSaveSumAndCountStatisticsData(
                        toSatisfactionSumAndCountStatisticsData(toMBTISatisfactionDtoList(satisfactionSaveSumsAndCounts, mbtiFactor)))
                .build();
    }
}
