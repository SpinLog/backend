package com.example.spinlog.statistics.dto.cache;

import lombok.Builder;

import java.util.Map;

@Builder
public record AllStatisticsMap(
        // todo 필드 이름 수정
        CountsAndSums genderEmotionAmountSpendCountsAndSums,
        CountsAndSums genderEmotionAmountSaveCountsAndSums,
        Map<String, Object> genderDailyAmountSpendSums,
        Map<String, Object> genderDailyAmountSaveSums,
        CountsAndSums genderSatisfactionSpendCountsAndSums,
        CountsAndSums genderSatisfactionSaveCountsAndSums) {
    @Override
    public String toString() {
        return "AllStatisticsMap{" +
                "\ngenderEmotionAmountSpendCountsAndSums=" + genderEmotionAmountSpendCountsAndSums +
                "\n\ngenderEmotionAmountSaveCountsAndSums=" + genderEmotionAmountSaveCountsAndSums +
                "\n\ngenderDailyAmountSpendSums=" + genderDailyAmountSpendSums +
                "\n\ngenderDailyAmountSaveSums=" + genderDailyAmountSaveSums +
                "\n\ngenderSatisfactionSpendCountsAndSums=" + genderSatisfactionSpendCountsAndSums +
                "\n\ngenderSatisfactionSaveCountsAndSums=" + genderSatisfactionSaveCountsAndSums +
                "\n}";
    }
}