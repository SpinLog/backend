package com.example.spinlog.statistics.dto;

import com.example.spinlog.user.entity.Gender;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class GenderSatisfactionSumAndCountDto {
    private Gender gender;
    private Double satisfactionSum;
    private Long satisfactionCount;
}
