package com.example.spinlog.statistics.dto;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class DailyAmountSumDto {
    private LocalDate localDate;
    private Long amountSum;
}
