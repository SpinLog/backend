package com.example.spinlog.statistics.dto;

import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GenderDataDto<T extends Number> {
    private Gender gender;
    private T value;
}
