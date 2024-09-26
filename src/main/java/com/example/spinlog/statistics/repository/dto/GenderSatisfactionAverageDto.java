package com.example.spinlog.statistics.repository.dto;

import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class GenderSatisfactionAverageDto {
    private Gender gender;
    private Float satisfactionAverage;
}
