package com.example.spinlog.statistics.repository.dto;

import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GenderDataDto<T extends Number> {
    private Gender gender;
    private T value;
}
