package com.example.spinlog.statistics.dto.cache;

import lombok.Builder;

import java.util.Map;

@Builder
public record AllGenderStatisticsCacheData(
        SumAndCountStatisticsData<Long> genderEmotionAmountSpendSumAndCountStatisticsData,
        SumAndCountStatisticsData<Long> genderEmotionAmountSaveSumAndCountStatisticsData,
        Map<String, Long> genderDailyAmountSpendSums,
        Map<String, Long> genderDailyAmountSaveSums,
        SumAndCountStatisticsData<Double> genderSatisfactionSpendSumAndCountStatisticsData,
        SumAndCountStatisticsData<Double> genderSatisfactionSaveSumAndCountStatisticsData) {
    @Override
    public String toString() {
        return "AllGenderStatisticsCacheData{" +
                "\ngenderEmotionAmountSpendSumsAndCounts=" + genderEmotionAmountSpendSumAndCountStatisticsData +
                "\n\ngenderEmotionAmountSaveSumsAndCounts=" + genderEmotionAmountSaveSumAndCountStatisticsData +
                "\n\ngenderDailyAmountSpendSums=" + genderDailyAmountSpendSums +
                "\n\ngenderDailyAmountSaveSums=" + genderDailyAmountSaveSums +
                "\n\ngenderSatisfactionSpendSumsAndCounts=" + genderSatisfactionSpendSumAndCountStatisticsData +
                "\n\ngenderSatisfactionSaveSumsAndCounts=" + genderSatisfactionSaveSumAndCountStatisticsData +
                "\n}";
    }
}