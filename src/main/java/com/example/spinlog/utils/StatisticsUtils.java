package com.example.spinlog.utils;

import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderDataDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsUtils {
    public static final int PERIOD_CRITERIA = 30;

    public static Map<String, Object> toGenderEmotionMap(List<GenderEmotionAmountAverageDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getEmotion(),
                        GenderEmotionAmountAverageDto::getAmountAverage));
    }

    public static Map<String, Object> toGenderDateMap(List<GenderDailyAmountSumDto> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender() + "::" + dto.getLocalDate(),
                        GenderDailyAmountSumDto::getAmountSum));
    }

    public static <T extends Number> Map<String, Object> toGenderMap(List<GenderDataDto<T>> dtos){
        return dtos.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getGender().toString(),
                        GenderDataDto::getValue));
    }
}
