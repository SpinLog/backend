package com.example.spinlog.user.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Getter
public class UpdateUserRequestDto {

    private final String mbti;

    private final String gender;

    private final Integer budget;

}
