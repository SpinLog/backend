package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.repository.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MBTIStatisticsRepository {
    // mbti별, 감정별 지출 평균 그래프
    List<MBTIEmotionAmountAverageDto> getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // mbti별, 날짜별 지출 합 그래프
    List<MBTIDailyAmountSumDto> getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 모든 메모 가져오기
    List<MemoDto> getAllMemosByMBTIBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("mbti") String mbti,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // mbti별 만족도 평균 그래프
    List<MBTISatisfactionAverageDto> getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ---

    List<MBTIEmotionAmountSumAndCountDto> getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<MBTISatisfactionSumAndCountDto> getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate(
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
