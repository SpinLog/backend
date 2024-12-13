package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.DailyAmountSumDto;
import com.example.spinlog.statistics.dto.EmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.SatisfactionSumAndCountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SpecificUserStatisticsRepository {
    List<EmotionAmountSumAndCountDto> getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
            @Param("userId") Long userId,
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<DailyAmountSumDto> getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
            @Param("userId") Long userId,
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<SatisfactionSumAndCountDto> getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
            @Param("userId") Long userId,
            @Param("registerType") RegisterType registerType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
