package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@Transactional(readOnly = true)
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

    public AllStatisticsResult getGenderStatisticsAllDataByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendSums = genderStatisticsRepository
                .getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSpendCounts = genderStatisticsRepository
                .getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveSums = genderStatisticsRepository
                .getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);
        List<GenderEmotionAmountAverageDto> genderEmotionAmountSaveCounts = genderStatisticsRepository
                .getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<GenderDailyAmountSumDto> genderDailyAmountSpendSums = genderStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderDailyAmountSumDto> genderDailyAmountSaveSums = genderStatisticsRepository
                .getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        List<GenderDataDto<Double>> genderSatisfactionSpendSums = genderStatisticsRepository
                .getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderDataDto<Long>> genderSatisfactionSpendCounts = genderStatisticsRepository
                .getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SPEND, startDate, endDate);
        List<GenderDataDto<Double>> genderSatisfactionSaveSums = genderStatisticsRepository
                .getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);
        List<GenderDataDto<Long>> genderSatisfactionSaveCounts = genderStatisticsRepository
                .getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(
                        userId, SAVE, startDate, endDate);

        return AllStatisticsResult.builder()
                .genderDailyAmountSaveSums(genderDailyAmountSaveSums)
                .genderDailyAmountSpendSums(genderDailyAmountSpendSums)
                .genderEmotionAmountSaveCounts(genderEmotionAmountSaveCounts)
                .genderEmotionAmountSaveSums(genderEmotionAmountSaveSums)
                .genderEmotionAmountSpendCounts(genderEmotionAmountSpendCounts)
                .genderEmotionAmountSpendSums(genderEmotionAmountSpendSums)
                .genderSatisfactionSaveCounts(genderSatisfactionSaveCounts)
                .genderSatisfactionSaveSums(genderSatisfactionSaveSums)
                .genderSatisfactionSpendCounts(genderSatisfactionSpendCounts)
                .genderSatisfactionSpendSums(genderSatisfactionSpendSums)
                .build();

    }

    // todo 별도의 클래스로 분리
    public record CountsAndSums(Map<String, Object> sumsMap, Map<String, Object> countsMap) {
        @Override
        public String toString() {
            return "CountsAndSums{" +
                    "\n\tsumsMap=" + sumsMap +
                    "\n\tcountsMap=" + countsMap +
                    '}';
        }
    }
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
}
