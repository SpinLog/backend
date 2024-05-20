package com.example.spinlog.statistics.controller;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.response.ApiResponseWrapper;
import com.example.spinlog.global.response.ResponseUtils;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.service.GenderStatisticsService;
import com.example.spinlog.statistics.service.dto.GenderDailyAmountSumResponse;
import com.example.spinlog.statistics.service.dto.GenderEmotionAmountAverageResponse;
import com.example.spinlog.statistics.service.dto.GenderWordFrequencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GenderStatisticsController {
    private final GenderStatisticsService genderStatisticsService;
    @GetMapping("/api/statistics/gender/emotion/amounts/average")
    public ApiResponseWrapper<List<GenderEmotionAmountAverageResponse>> getAmountAveragesEachGenderAndEmotionLast90Days(
            @RequestParam(defaultValue = "SPEND") String registerType){
        return ResponseUtils.ok(
                genderStatisticsService.getAmountAveragesEachGenderAndEmotionLast90Days(
                        LocalDate.now(),
                        RegisterType.valueOf(registerType)),
                "성별 감정별 금액 평균");
    }

    @GetMapping("/api/statistics/gender/daily/amounts/sum")
    public ApiResponseWrapper<List<GenderDailyAmountSumResponse>> getAmountSumsEachGenderAndDayLast90Days(
            @RequestParam(defaultValue = "SPEND") String registerType){
        return ResponseUtils.ok(
                genderStatisticsService.getAmountSumsEachGenderAndDayLast90Days(
                        LocalDate.now(),
                        RegisterType.valueOf(registerType)),
                "성별 일별 금액 총합");
    }

    @GetMapping("/api/statistics/gender/word/frequencies")
    public ApiResponseWrapper<GenderWordFrequencyResponse> getWordFrequencyEachGenderLast90Days(
            @RequestParam(defaultValue = "SPEND") String registerType){
        return ResponseUtils.ok(
                genderStatisticsService
                        .getWordFrequenciesEachGenderLast90Days(
                                LocalDate.now(),
                                RegisterType.valueOf(registerType)),
                "성별 단어 빈도수");
    }

    @GetMapping("/api/statistics/gender/satisfactions/average")
    public ApiResponseWrapper<List<GenderSatisfactionAverageDto>> getSatisfactionAveragesEachGenderLast90Days(
            @RequestParam(defaultValue = "SPEND") String registerType){
        return ResponseUtils.ok(
                genderStatisticsService.getSatisfactionAveragesEachGenderLast90Days(
                        LocalDate.now(),
                        RegisterType.valueOf(registerType)),
                "성별 만족도 평균");
    }
}
