package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.statistics.dto.DailyAmountSumDto;
import com.example.spinlog.statistics.dto.EmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.SatisfactionSumAndCountDto;
import lombok.Builder;

import java.util.List;

@Builder
public class AllMBTIStatisticsRepositoryData { // todo : rename
    List<EmotionAmountSumAndCountDto> emotionAmountSpendSumsAndCounts;
    List<EmotionAmountSumAndCountDto> emotionAmountSaveSumsAndCounts;
    List<DailyAmountSumDto> dailyAmountSpendSums;
    List<DailyAmountSumDto> dailyAmountSaveSums;
    List<SatisfactionSumAndCountDto> satisfactionSpendSumsAndCounts;
    List<SatisfactionSumAndCountDto> satisfactionSaveSumsAndCounts;
}
