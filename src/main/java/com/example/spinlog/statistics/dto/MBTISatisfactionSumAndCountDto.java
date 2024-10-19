package com.example.spinlog.statistics.dto;

import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MBTISatisfactionSumAndCountDto {
    private MBTIFactor mbtiFactor;
    private Double satisfactionSum;
    private Long satisfactionCount;
}
