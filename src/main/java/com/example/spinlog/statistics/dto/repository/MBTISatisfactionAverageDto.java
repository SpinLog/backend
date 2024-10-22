package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class MBTISatisfactionAverageDto {
    private MBTIFactor mbtiFactor;
    private Float satisfactionAverage;
}
