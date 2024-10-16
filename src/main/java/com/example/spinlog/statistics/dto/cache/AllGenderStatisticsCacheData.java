package com.example.spinlog.statistics.dto.cache;

import lombok.Builder;

import java.util.Map;

@Builder
public record AllGenderStatisticsCacheData(
        // todo 필드 이름 수정
        SumsAndCounts genderEmotionAmountSpendSumsAndCounts,
        SumsAndCounts genderEmotionAmountSaveSumsAndCounts,
        Map<String, Object> genderDailyAmountSpendSums,
        Map<String, Object> genderDailyAmountSaveSums,
        SumsAndCounts genderSatisfactionSpendSumsAndCounts,
        SumsAndCounts genderSatisfactionSaveSumsAndCounts) {
    @Override
    public String toString() {
        return "AllGenderStatisticsCacheData{" +
                "\ngenderEmotionAmountSpendSumsAndCounts=" + genderEmotionAmountSpendSumsAndCounts +
                "\n\ngenderEmotionAmountSaveSumsAndCounts=" + genderEmotionAmountSaveSumsAndCounts +
                "\n\ngenderDailyAmountSpendSums=" + genderDailyAmountSpendSums +
                "\n\ngenderDailyAmountSaveSums=" + genderDailyAmountSaveSums +
                "\n\ngenderSatisfactionSpendSumsAndCounts=" + genderSatisfactionSpendSumsAndCounts +
                "\n\ngenderSatisfactionSaveSumsAndCounts=" + genderSatisfactionSaveSumsAndCounts +
                "\n}";
    }
}