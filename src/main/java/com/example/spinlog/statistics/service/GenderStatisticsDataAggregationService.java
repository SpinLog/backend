package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.utils.StatisticsUtils.*;
import static com.example.spinlog.utils.StatisticsUtils.toGenderMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsDataAggregationService {
    private final GenderStatisticsRepository genderStatisticsRepository;

    public AllStatisticsMap getGenderStatisticsAllData(LocalDate startDate, LocalDate endDate) {
        CountsAndSums genderEmotionAmountSpendCountsAndSums = getGenderEmotionAmountCountsAndSums(SPEND, startDate, endDate);
        CountsAndSums genderEmotionAmountSaveCountsAndSums = getGenderEmotionAmountCountsAndSums(SAVE, startDate, endDate);

        Map<String, Object> genderDailyAmountSpendSums = getGenderDateAmountSums(SPEND, startDate, endDate);
        Map<String, Object> genderDailyAmountSaveSums = getGenderDateAmountSums(SAVE, startDate, endDate);

        CountsAndSums genderSatisfactionSpendCountsAndSums = getGenderSatisfactionCountsAndSums(SPEND, startDate, endDate);
        CountsAndSums genderSatisfactionSaveCountsAndSums = getGenderSatisfactionCountsAndSums(SAVE, startDate, endDate);

        return AllStatisticsMap.builder()
                .genderEmotionAmountSpendCountsAndSums(genderEmotionAmountSpendCountsAndSums)
                .genderEmotionAmountSaveCountsAndSums(genderEmotionAmountSaveCountsAndSums)
                .genderDailyAmountSpendSums(genderDailyAmountSpendSums)
                .genderDailyAmountSaveSums(genderDailyAmountSaveSums)
                .genderSatisfactionSpendCountsAndSums(genderSatisfactionSpendCountsAndSums)
                .genderSatisfactionSaveCountsAndSums(genderSatisfactionSaveCountsAndSums)
                .build();
    };
    public CountsAndSums getGenderEmotionAmountCountsAndSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderEmotionAmountAverageDto> amountSums = genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        List<GenderEmotionAmountAverageDto> amountCounts = genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        Map<String, Object> amountSumsMap = toGenderEmotionMap(amountSums);
        Map<String, Object> amountCountsMap = toGenderEmotionMap(amountCounts);

        return new CountsAndSums(amountSumsMap, amountCountsMap);
    }

    public Map<String, Object> getGenderDateAmountSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderDailyAmountSumDto> amountSums = genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        return toGenderDateMap(amountSums);
    }

    public CountsAndSums getGenderSatisfactionCountsAndSums(RegisterType registerType, LocalDate startDate, LocalDate endDate) {
        List<GenderDataDto<Double>> satisfactionSums = genderStatisticsRepository.getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);
        List<GenderDataDto<Long>> satisfactionCounts = genderStatisticsRepository.getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
                registerType,
                startDate,
                endDate);

        Map<String, Object> satisfactionSumsMap = toGenderMap(satisfactionSums);
        Map<String, Object> satisfactionCountsMap = toGenderMap(satisfactionCounts);

        return new CountsAndSums(satisfactionSumsMap, satisfactionCountsMap);
    }

    public record CountsAndSums(Map<String, Object> sumsMap, Map<String, Object> countsMap) { }
    @Builder
    public record AllStatisticsMap(
            CountsAndSums genderEmotionAmountSpendCountsAndSums,
            CountsAndSums genderEmotionAmountSaveCountsAndSums,
            Map<String, Object> genderDailyAmountSpendSums,
            Map<String, Object> genderDailyAmountSaveSums,
            CountsAndSums genderSatisfactionSpendCountsAndSums,
            CountsAndSums genderSatisfactionSaveCountsAndSums) { }
}
