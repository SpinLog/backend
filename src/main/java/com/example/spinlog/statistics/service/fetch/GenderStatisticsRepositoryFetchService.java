package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsRepositoryFetchService {
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

    // todo convert 하지 않고 그대로 반환 (재사용성)
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

    public AllStatisticsMap getGenderStatisticsAllDataByGender(Gender gender, LocalDate startDate, LocalDate endDate) {
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendSums = genderStatisticsRepository
                .getAmountSumsEachEmotionByGenderBetweenStartDateAndEndDate(
                        gender, SPEND, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendCounts = genderStatisticsRepository
                .getAmountCountsEachEmotionByGenderBetweenStartDateAndEndDate(
                        gender, SPEND, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveSums = genderStatisticsRepository
                .getAmountSumsEachEmotionByGenderBetweenStartDateAndEndDate(
                        gender, SAVE, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveCounts = genderStatisticsRepository
                .getAmountCountsEachEmotionByGenderBetweenStartDateAndEndDate(
                        gender, SAVE, startDate, endDate);

        List<GenderDailyAmountSumDto> genderDailyAmountSpendSums = genderStatisticsRepository
                .getAmountSumsEachDayByGenderBetweenStartDateAndEndDate(
                        gender, SPEND, startDate, endDate);
        List<GenderDailyAmountSumDto> genderDailyAmountSaveSums = genderStatisticsRepository
                .getAmountSumsEachDayByGenderBetweenStartDateAndEndDate(
                        gender, SAVE, startDate, endDate);

        List<GenderDataDto<Double>> genderSatisfactionSpendSums = genderStatisticsRepository
                .getSatisfactionSumsByGenderBetweenStartDateAndEndDate(
                        gender, SPEND, startDate, endDate);
        List<GenderDataDto<Long>> genderSatisfactionSpendCounts = genderStatisticsRepository
                .getSatisfactionCountsByGenderBetweenStartDateAndEndDate(
                        gender, SPEND, startDate, endDate);
        List<GenderDataDto<Double>> genderSatisfactionSaveSums = genderStatisticsRepository
                .getSatisfactionSumsByGenderBetweenStartDateAndEndDate(
                        gender, SAVE, startDate, endDate);
        List<GenderDataDto<Long>> genderSatisfactionSaveCounts = genderStatisticsRepository
                .getSatisfactionCountsByGenderBetweenStartDateAndEndDate(
                        gender, SAVE, startDate, endDate);

        return AllStatisticsMap.builder()
                .genderEmotionAmountSpendCountsAndSums(
                        new CountsAndSums(toGenderEmotionMap(genderEmotionAmountSpendSums),
                                toGenderEmotionMap(genderEmotionAmountSpendCounts)))
                .genderEmotionAmountSaveCountsAndSums(
                        new CountsAndSums(toGenderEmotionMap(genderEmotionAmountSaveSums),
                                toGenderEmotionMap(genderEmotionAmountSaveCounts)))
                .genderDailyAmountSpendSums(toGenderDateMap(genderDailyAmountSpendSums))
                .genderDailyAmountSaveSums(toGenderDateMap(genderDailyAmountSaveSums))
                .genderSatisfactionSpendCountsAndSums(
                        new CountsAndSums(toGenderMap(genderSatisfactionSpendSums),
                                toGenderMap(genderSatisfactionSpendCounts)))
                .genderSatisfactionSaveCountsAndSums(
                        new CountsAndSums(toGenderMap(genderSatisfactionSaveSums),
                                toGenderMap(genderSatisfactionSaveCounts)))
                .build();

    }

    // todo 별도의 클래스로 분리
    public record CountsAndSums(Map<String, Object> sumsMap, Map<String, Object> countsMap) { }
    @Builder
    public record AllStatisticsMap(
            // todo 필드 이름 수정
            CountsAndSums genderEmotionAmountSpendCountsAndSums,
            CountsAndSums genderEmotionAmountSaveCountsAndSums,
            Map<String, Object> genderDailyAmountSpendSums,
            Map<String, Object> genderDailyAmountSaveSums,
            CountsAndSums genderSatisfactionSpendCountsAndSums,
            CountsAndSums genderSatisfactionSaveCountsAndSums) { }
}
