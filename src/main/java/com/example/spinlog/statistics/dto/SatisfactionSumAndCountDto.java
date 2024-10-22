package com.example.spinlog.statistics.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class SatisfactionSumAndCountDto {
    private Double satisfactionSum;
    private Long satisfactionCount;
}
