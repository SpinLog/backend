package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MBTIDataDto<T> {
    private MBTIFactor mbtiFactor;
    private T value;
}
