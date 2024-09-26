package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.repository.dto.MemoDto;
import com.example.spinlog.user.entity.Gender;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface GenderStatisticsRepository {
    // todo avg round 작업 WAS에서 처리

    // 성별, 감정별 지출 평균 그래프
    List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotionBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 성별, 날짜별 지출 합 그래프
    List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 성별 메모 가져오기
    List<MemoDto> getAllMemosByGenderBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("gender") Gender gender,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 성별 만족도 평균 그래프
    List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGenderBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // todo 별도 클래스로 분리
    List<GenderEmotionAmountAverageDto> getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<GenderEmotionAmountAverageDto> getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<GenderDataDto<Double>> getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<GenderDataDto<Long>> getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
