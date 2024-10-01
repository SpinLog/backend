package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.utils.StatisticsCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheFallbackService {
    private final GenderStatisticsCacheFetchService genderStatisticsCacheFetchService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;

    public List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotion(RegisterType registerType){
        try {
            return genderStatisticsCacheFetchService
                    .getAmountAveragesEachGenderAndEmotion(registerType);
        } catch(InvalidCacheException e) {
            log.warn("GenderEmotionAmountAverage Cache fallback occurred. Query Database and Cache will be updated.", e);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
            CountsAndSums genderEmotionAmountCountsAndSums = genderStatisticsRepositoryFetchService
                    .getGenderEmotionAmountCountsAndSums(registerType, startDate, endDate);

            genderStatisticsCacheWriteService.putAmountCountsAndSumsByGenderAndEmotion(genderEmotionAmountCountsAndSums, registerType);

            return convertToGenderEmotionAmountAverageDto(
                    genderEmotionAmountCountsAndSums);
        }
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        try {
            return genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(registerType);
        } catch(InvalidCacheException e) {
            log.warn("GenderDailyAmountSum Cache fallback occurred. Query Database and Cache will be updated.", e);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
            Map<String, Object> genderDailyAmountSums = genderStatisticsRepositoryFetchService
                    .getGenderDateAmountSums(registerType, startDate, endDate);

            genderStatisticsCacheWriteService.putAmountSumsByGenderAndDate(genderDailyAmountSums, registerType);

            return convertToGenderDailyAmountSumDto(genderDailyAmountSums);
        }
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGender(RegisterType registerType) {
        try {
            return genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(registerType);
        } catch(InvalidCacheException e) {
            log.warn("GenderSatisfactionAverage Cache fallback occurred. Query Database and Cache will be updated.", e);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
            CountsAndSums genderSatisfactionCountsAndSums = genderStatisticsRepositoryFetchService
                    .getGenderSatisfactionCountsAndSums(registerType, startDate, endDate);

            genderStatisticsCacheWriteService.putSatisfactionCountsAndSumsByGender(genderSatisfactionCountsAndSums, registerType);

            return convertToGenderSatisfactionAverageDto(
                    genderSatisfactionCountsAndSums);
        }
    }
}
