package com.example.spinlog.statistics.dto;

import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MBTIDailyAmountSumDto {
    private MBTIFactor mbtiFactor;
    private LocalDate localDate;
    private Long amountSum;
}
