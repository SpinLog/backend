package com.example.spinlog.user.dto.response;

import com.example.spinlog.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class ViewUserResponseDto {

    private final String email;

    private final String mbti;

    private final String gender;

    private final Integer budget;

    public static ViewUserResponseDto of(User user) {
        return ViewUserResponseDto.builder()
                .email(user.getEmail())
                .mbti(user.getMbti().name())
                .gender(user.getGender().name())
                .budget(user.getBudget())
                .build();
    }
}
