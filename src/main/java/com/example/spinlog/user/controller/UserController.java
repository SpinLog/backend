package com.example.spinlog.user.controller;

import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.dto.response.UserResponseApi;
import com.example.spinlog.user.dto.response.ViewUserResponseDto;
import com.example.spinlog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 회원 정보 조회
     * @param userId 회원의 pk
     * @return 회원 정보 (email, mbti, gender, budget) 를 담은 ResponseApi
     */
    @GetMapping("/details")
    public ResponseEntity<UserResponseApi<ViewUserResponseDto>> viewDetails(
            @SessionAttribute("userId") Long userId //TODO security 사용
    ) {
        ViewUserResponseDto responseDto = userService.findUser(userId);
        log.info("User {} 정보 조회 성공 : {}", responseDto.getEmail(), responseDto);

        UserResponseApi<ViewUserResponseDto> responseApi = UserResponseApi.success(responseDto, "User 정보 조회에 성공했습니다.");

        return ResponseEntity.ok(responseApi);
    }


    /**
     * 회원 정보 저장 및 수정
     * @param userId 회원의 pk
     * @param requestDto 수정할 회원 정보 (mbti, gender, budget)
     * @return 성공 또는 실패 ResponseApi
     */
    @PostMapping("/details")
    public ResponseEntity<UserResponseApi<?>> storeDetails(
            @SessionAttribute("userId") Long userId, //TODO security 사용
            @RequestBody UpdateUserRequestDto requestDto
    ) {
        userService.updateUserInfo(userId, requestDto);

        UserResponseApi<Object> responseApi = UserResponseApi.success("User 정보 저장에 성공했습니다.");

        return ResponseEntity.ok(responseApi);
    }

}
