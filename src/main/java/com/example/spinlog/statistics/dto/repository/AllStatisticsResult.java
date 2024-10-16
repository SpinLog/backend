package com.example.spinlog.statistics.dto.repository;


import lombok.Builder;

import java.util.List;

@Builder
public record AllStatisticsResult(
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendSums,
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendCounts,
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveSums,
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveCounts,
        List<GenderDailyAmountSumDto> genderDailyAmountSpendSums,
        List<GenderDailyAmountSumDto> genderDailyAmountSaveSums,
        List<GenderDataDto<Double>> genderSatisfactionSpendSums,
        List<GenderDataDto<Long>> genderSatisfactionSpendCounts,
        List<GenderDataDto<Double>> genderSatisfactionSaveSums,
        List<GenderDataDto<Long>> genderSatisfactionSaveCounts) {}
