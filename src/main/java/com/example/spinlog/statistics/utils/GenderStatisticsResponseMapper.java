package com.example.spinlog.statistics.utils;

import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.dto.response.GenderDailyAmountSumResponse;
import com.example.spinlog.statistics.dto.response.GenderEmotionAmountAverageResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenderStatisticsResponseMapper {

    public static List<GenderEmotionAmountAverageResponse> toGenderEmotionAmountAverageResponse(List<GenderEmotionAmountAverageDto> dtos) {
        return dtos.stream()
                .map(d -> new GenderEmotionAmountAverageDto(d.getGender(), d.getEmotion(), roundForApiResponse(d.getAmountAverage())))
                .collect(
                        groupingBy(GenderEmotionAmountAverageDto::getGender))
                .entrySet().stream()
                .map((e) ->
                        GenderEmotionAmountAverageResponse.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(GenderEmotionAmountAverageResponse::getGender))
                .toList();
    }

    private static Long roundForApiResponse(Long amountAverage) {
        return (amountAverage/1000)*1000;
    }

    public static List<GenderDailyAmountSumResponse> toGenderDailyAmountSumResponse(List<GenderDailyAmountSumDto> dtosWithZeroPadding) {
        return dtosWithZeroPadding.stream()
                .collect(
                        groupingBy(GenderDailyAmountSumDto::getGender))
                .entrySet().stream()
                .map((e) ->
                        GenderDailyAmountSumResponse.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(GenderDailyAmountSumResponse::getGender))
                .toList();
    }

    public static List<GenderSatisfactionAverageDto> toGenderSatisfactionAmountAverageResponse(List<GenderSatisfactionAverageDto> dtos) {
        return dtos.stream()
                .map(d ->
                        GenderSatisfactionAverageDto.builder()
                                .gender(d.getGender())
                                .satisfactionAverage(roundForApiResponse(d.getSatisfactionAverage()))
                                .build())
                .toList();
    }

    private static Float roundForApiResponse(Float satisfactionAverage) {
        String s = String.format("%.1f", satisfactionAverage);
        return Float.valueOf(s);
    }
}
