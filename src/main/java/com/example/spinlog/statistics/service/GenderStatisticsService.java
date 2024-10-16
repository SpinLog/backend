package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.response.*;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheFallbackService;
import com.example.spinlog.statistics.service.wordanalysis.WordExtractionService;
import com.example.spinlog.user.entity.Gender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.example.spinlog.statistics.utils.GenderStatisticsResponseMapper.*;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.PERIOD_CRITERIA;
import static com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils.*;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@Transactional(readOnly = true) // todo 범위 좁히기
public class GenderStatisticsService {
    private final GenderStatisticsRepository genderStatisticsRepository;
    private final GenderStatisticsCacheFallbackService genderStatisticsCacheFallbackService;
    private final WordExtractionService wordExtractionService;

    public GenderStatisticsService(GenderStatisticsRepository genderStatisticsRepository, GenderStatisticsCacheFallbackService genderStatisticsCacheFallbackService, WordExtractionService wordExtractionService) {
        this.genderStatisticsRepository = genderStatisticsRepository;
        this.genderStatisticsCacheFallbackService = genderStatisticsCacheFallbackService;
        this.wordExtractionService = wordExtractionService;
    }

    public List<GenderEmotionAmountAverageResponse> getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType registerType){
        List<GenderEmotionAmountAverageDto> dtos = genderStatisticsCacheFallbackService.
                getAmountAveragesEachGenderAndEmotion(registerType);
        List<GenderEmotionAmountAverageDto> dtosWithZeroPadding = addZeroAverageForMissingGenderEmotionPairs(dtos);
        return toGenderEmotionAmountAverageResponse(dtosWithZeroPadding);
    }

    public List<GenderDailyAmountSumResponse> getAmountSumsEachGenderAndDayLast30Days(RegisterType registerType) {
        List<GenderDailyAmountSumDto> dtos = genderStatisticsCacheFallbackService
                .getAmountSumsEachGenderAndDay(registerType);
        List<GenderDailyAmountSumDto> dtosWithZeroPadding = addZeroAverageForMissingGenderLocalDatePairs(dtos);
        return toGenderDailyAmountSumResponse(dtosWithZeroPadding);
    }

    public GenderWordFrequencyResponse getWordFrequenciesEachGenderLast30Days(RegisterType registerType){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(PERIOD_CRITERIA);
        List<MemoDto> maleMemos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(registerType, Gender.MALE, startDate, today);
        List<MemoDto> femaleMemos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(registerType, Gender.FEMALE, startDate, today);

        return GenderWordFrequencyResponse.builder()
                .maleWordFrequencies(
                        wordExtractionService.analyzeWords(
                                maleMemos.stream()
                                        .flatMap((m) -> Stream.of(
                                                m.getContent(),
                                                m.getEvent(),
                                                m.getReason(),
                                                m.getThought(),
                                                m.getImprovements()))
                                        .filter(Objects::nonNull)
                                        .toList()))
                .femaleWordFrequencies(
                        wordExtractionService.analyzeWords(
                                femaleMemos.stream()
                                        .flatMap((m) -> Stream.of(
                                                m.getContent(),
                                                m.getEvent(),
                                                m.getReason(),
                                                m.getThought(),
                                                m.getImprovements()))
                                        .filter(Objects::nonNull)
                                        .toList()))
                .build();
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGenderLast30Days(RegisterType registerType){
        List<GenderSatisfactionAverageDto> dtos = genderStatisticsCacheFallbackService.getSatisfactionAveragesEachGender(registerType);
        return toGenderSatisfactionAmountAverageResponse(dtos);
    }
}
