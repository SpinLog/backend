package com.example.spinlog.statistics.dto;

import com.example.spinlog.user.entity.Gender;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class GenderDailyAmountSumDto {
    private Gender gender;
    private LocalDate localDate;
    private Long amountSum;
}
