package com.example.spinlog.user.controller;

import com.example.spinlog.global.response.ApiResponseWrapper;
import com.example.spinlog.global.response.ResponseUtils;
import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.dto.response.ViewUserResponseDto;
import com.example.spinlog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 회원 정보 조회
     *
     * @return 회원 정보 (email, mbti, gender, budget) 를 담은 ResponseApi
     */
    @GetMapping("/details")
    public ApiResponseWrapper<ViewUserResponseDto> viewDetails() {
        ViewUserResponseDto responseDto = userService.findUser();

        log.info("User {} 정보 조회 성공 : {}", responseDto.getEmail(), responseDto);

        return ResponseUtils.ok(responseDto, "User 정보 조회에 성공했습니다.");
    }


    /**
     * 회원 정보 저장 및 수정
     *
     * @param oAuth2User OAuth2 Provider 로부터 넘겨받은 회원의 정보
     * @param requestDto 수정할 회원 정보 (mbti, gender, budget)
     * @return 성공 또는 실패 ResponseApi
     */
    @PostMapping("/details")
    public ApiResponseWrapper<Object> storeDetails(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestBody UpdateUserRequestDto requestDto
    ) {
        String authenticationName = oAuth2User.getOAuth2Response().getAuthenticationName();
        userService.updateUserInfo(authenticationName, requestDto);

        return ResponseUtils.ok("User 정보 저장에 성공했습니다.");
    }

}
