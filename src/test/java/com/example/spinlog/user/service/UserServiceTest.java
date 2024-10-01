package com.example.spinlog.user.service;

import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import com.example.spinlog.user.custom.securitycontext.WithMockCustomOAuth2User;
import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.dto.response.ViewUserResponseDto;
import com.example.spinlog.user.entity.Budget;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.spinlog.user.custom.securitycontext.OAuth2Provider.KAKAO;
import static com.example.spinlog.user.custom.securitycontext.OAuth2Provider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("회원 정보 비즈니스 로직")
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration
class UserServiceTest {

    @InjectMocks private UserService userService;

    @Mock private UserRepository userRepository;

    @Mock ApplicationEventPublisher eventPublisher;

    @DisplayName("회원 정보 조회")
    @Test
    @WithMockCustomOAuth2User(
            provider = KAKAO, email = "kakaoemail@kakao.com", providerMemberId = "123ab", isFirstLogin = false
    )
    void givenUser_whenFindUser_thenReturnsCorrectly() {
        // Given
        CustomOAuth2User oAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String authenticationName = oAuth2User.getOAuth2Response().getAuthenticationName();
        User user = User.builder()
                .email(oAuth2User.getName())
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .authenticationName(authenticationName)
                .build();

        user.addCurrentMonthBudget(12345, LocalDate.now());

        when(userRepository.findByAuthenticationName(authenticationName))
                .thenReturn(Optional.of(user));

        // When
        ViewUserResponseDto responseDto = userService.findUser();

        // Then
        assertThat(responseDto)
                .hasFieldOrPropertyWithValue("email", "kakaoemail@kakao.com")
                .hasFieldOrPropertyWithValue("mbti", Mbti.ISTP.name())
                .hasFieldOrPropertyWithValue("gender", Gender.MALE.name())
                .hasFieldOrPropertyWithValue("budget", 12345);
        verify(userRepository, times(1)).findByAuthenticationName(authenticationName);
    }

    @DisplayName("회원 정보 저장 및 변경")
    @Test
    @WithMockCustomOAuth2User(
            provider = NAVER, email = "naveremail@kakao.com", providerMemberId = "abcde3456", isFirstLogin = false
    )
    void givenUser_whenUpdateUserInfo_thenReturnsCorrectly() {
        // Given
        CustomOAuth2User oAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String authenticationName = oAuth2User.getOAuth2Response().getAuthenticationName();
        User user = User.builder()
                .email(oAuth2User.getName())
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .authenticationName(authenticationName)
                .build();

        Budget budget = user.addCurrentMonthBudget(67890, LocalDate.now());

        when(userRepository.findByAuthenticationName(authenticationName))
                .thenReturn(Optional.of(user));

        // When
        UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
                .mbti("ENFJ")
                .gender("FEMALE")
                .budget(67890)
                .build();
        userService.updateUserInfo(requestDto);

        // Then
        assertThat(user)
                .hasFieldOrPropertyWithValue("email", "naveremail@kakao.com")
                .hasFieldOrPropertyWithValue("mbti", Mbti.ENFJ)
                .hasFieldOrPropertyWithValue("gender", Gender.FEMALE);
        assertThat(budget)
                .hasFieldOrPropertyWithValue("budget", 67890);
        verify(userRepository, times(1)).findByAuthenticationName(authenticationName);
    }

}