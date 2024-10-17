package com.example.spinlog.statistics.dto.cache;

import lombok.Builder;

import java.util.Map;

@Builder
public record AllGenderStatisticsCacheData(
        SumAndCountStatisticsData genderEmotionAmountSpendSumAndCountStatisticsData,
        SumAndCountStatisticsData genderEmotionAmountSaveSumAndCountStatisticsData,
        Map<String, Object> genderDailyAmountSpendSums,
        Map<String, Object> genderDailyAmountSaveSums,
        SumAndCountStatisticsData genderSatisfactionSpendSumAndCountStatisticsData,
        SumAndCountStatisticsData genderSatisfactionSaveSumAndCountStatisticsData) {
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