package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.cache.SumsAndCounts;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheFallbackService {
    private final GenderStatisticsCacheFetchService genderStatisticsCacheFetchService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    public List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotion(RegisterType registerType){
        try {
            return convertToGenderEmotionAmountAverageDto(genderStatisticsCacheFetchService
                    .getAmountAveragesEachGenderAndEmotion(registerType));
        } catch(InvalidCacheException e) {
            log.warn("GenderEmotionAmountAverage Cache fallback occurred. Query Database and Cache will be updated.", e);

            Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            SumsAndCounts genderEmotionAmountSumsAndCounts = genderStatisticsRepositoryFetchService
                    .getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);

            genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(genderEmotionAmountSumsAndCounts, registerType);

            return convertToGenderEmotionAmountAverageDto(
                    genderEmotionAmountSumsAndCounts);
        }
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        try {
            return convertToGenderDailyAmountSumDto(
                    genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(registerType));
        } catch(InvalidCacheException e) {
            log.warn("GenderDailyAmountSum Cache fallback occurred. Query Database and Cache will be updated.", e);

            Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            Map<String, Object> genderDailyAmountSums = genderStatisticsRepositoryFetchService
                    .getGenderDateAmountSums(registerType, startDate, endDate);

            genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(genderDailyAmountSums, registerType);

            return convertToGenderDailyAmountSumDto(genderDailyAmountSums);
        }
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGender(RegisterType registerType) {
        try {
            return convertToGenderSatisfactionAverageDto(
                    genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(registerType));
        } catch(InvalidCacheException e) {
            log.warn("GenderSatisfactionAverage Cache fallback occurred. Query Database and Cache will be updated.", e);

            Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            SumsAndCounts genderSatisfactionSumsAndCounts = genderStatisticsRepositoryFetchService
                    .getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);

            genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(genderSatisfactionSumsAndCounts, registerType);

            return convertToGenderSatisfactionAverageDto(
                    genderSatisfactionSumsAndCounts);
        }
    }
}
