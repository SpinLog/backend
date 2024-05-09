package com.example.spinlog.user.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdateUserRequestDto {

    private String mbti;

    private String gender;

    private Integer budget;

    @Builder
    private UpdateUserRequestDto(String mbti, String gender, Integer budget) {
        this.mbti = mbti;
        this.gender = gender;
        this.budget = budget;
    }
}
