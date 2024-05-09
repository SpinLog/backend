package com.example.spinlog.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) //Json 으로 응답을 보낼 때 null 인 필드 제외
public class UserResponseApi<T> {

    private final Boolean success;

    private final String message;

    private final T data;

    public static <T> UserResponseApi<T> success(T responseDto, String message) {
        return UserResponseApi.<T>builder()
                .success(true)
                .message(message)
                .data(responseDto)
                .build();
    }

    public static <T> UserResponseApi<T> successWithoutData(String message) {
        return UserResponseApi.<T>builder()
                .success(true)
                .message(message)
                .data(null)
                .build();
    }
}
