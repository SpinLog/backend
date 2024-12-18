package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.user.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GenderDataDto<T extends Number> {
    private Gender gender;
    private T value;
}
