package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class GenderSatisfactionAverageDto {
    private Gender gender;
    private Float satisfactionAverage;
}
