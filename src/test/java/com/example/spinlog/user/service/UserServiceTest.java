package com.example.spinlog.user.service;

import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.dto.response.ViewUserResponseDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("비즈니스 로직 - 사용자")
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("회원 정보 조회 - 성공")
    @Test
    void givenUserId_whenFindUserByUserId_thenFindExactly() {
        // Given
        User user = User.builder()
                .email("asdf@gmail.com")
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .budget(99_999_999)
                .build();
        userRepository.save(user);
        Long userId = user.getId();

        // When
        ViewUserResponseDto responseDto = userService.findUser(userId);

        // Then
        assertThat(responseDto)
                .extracting("email", "mbti", "gender", "budget")
                .containsExactly("asdf@gmail.com", "ISTP", "MALE", 99_999_999);
    }

    @DisplayName("회원 정보 저장 및 수정 - 성공")
    @Test
    void givenUserIdAndUpdateValue_whenUpdateUserInfo_thenUpdateSuccessfully() {
        // Given
        User user = User.builder()
                .email("asdf@gmail.com")
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .budget(99_999_999)
                .build();
        userRepository.save(user);
        Long userId = user.getId();

        UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
                .mbti("ENFJ")
                .gender("FEMALE")
                .budget(123_456)
                .build();

        // When
        userService.updateUserInfo(userId, requestDto);

        // Then
        Optional<User> foundUser = userRepository.findById(userId);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).extracting("id", "email", "mbti", "gender", "budget")
                .containsExactly(1L, "asdf@gmail.com", Mbti.ENFJ, Gender.FEMALE, 123_456);
    }

}