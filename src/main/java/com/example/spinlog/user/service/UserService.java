package com.example.spinlog.user.service;

import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.dto.response.ViewUserResponseDto;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ViewUserResponseDto findUser(Long userId) {
        User foundUser = getUser(userId);

        return ViewUserResponseDto.of(foundUser);
    }

    @Transactional
    public void updateUserInfo(Long userId, UpdateUserRequestDto requestDto) {
        User foundUser = getUser(userId);

        foundUser.changeProfile(requestDto.getMbti(), requestDto.getGender() ,requestDto.getBudget());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("ID " + userId + "에 해당하는 사용자를 찾을 수 없습니다.")
        );
    }
}
