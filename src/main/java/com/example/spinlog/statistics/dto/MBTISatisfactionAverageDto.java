package com.example.spinlog.statistics.dto;

import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MBTISatisfactionAverageDto {
    private MBTIFactor mbtiFactor;
    private Float satisfactionAverage;
}
