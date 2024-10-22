package com.example.spinlog.statistics.dto.cache;

import lombok.Builder;

import java.util.Map;

@Builder
public record AllStatisticsCacheData(
        SumAndCountStatisticsData<Long> emotionAmountSpendSumAndCountStatisticsData,
        SumAndCountStatisticsData<Long> emotionAmountSaveSumAndCountStatisticsData,
        Map<String, Long> dailyAmountSpendSums,
        Map<String, Long> dailyAmountSaveSums,
        SumAndCountStatisticsData<Double> satisfactionSpendSumAndCountStatisticsData,
        SumAndCountStatisticsData<Double> satisfactionSaveSumAndCountStatisticsData) {
    @Override
    public String toString() {
        return "AllGenderStatisticsCacheData{" +
                "\nemotionAmountSpendSumsAndCounts=" + emotionAmountSpendSumAndCountStatisticsData +
                "\n\nemotionAmountSaveSumsAndCounts=" + emotionAmountSaveSumAndCountStatisticsData +
                "\n\ndailyAmountSpendSums=" + dailyAmountSpendSums +
                "\n\ndailyAmountSaveSums=" + dailyAmountSaveSums +
                "\n\nsatisfactionSpendSumsAndCounts=" + satisfactionSpendSumAndCountStatisticsData +
                "\n\nsatisfactionSaveSumsAndCounts=" + satisfactionSaveSumAndCountStatisticsData +
                "\n}";
    }
}