package com.example.spinlog.user.controller;

import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static com.example.spinlog.user.entity.Gender.FEMALE;
import static com.example.spinlog.user.entity.Mbti.ENFJ;
import static net.minidev.json.JSONValue.toJSONString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("회원 정보 컨트롤러")
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;

    @AfterEach
    void clearResources() {
        userRepository.deleteAll();
    }

    @DisplayName("[GET] 회원 정보 조회 - 정상 호출")
    @Test
    void givenUserAndSession_whenTryingRetrieveUserInfo_thenReturnsOK() throws Exception {
        // Given
        User user = User.builder()
                .email("asdf@gmail.com")
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .budget(99_999_999)
                .build();
        userRepository.save(user);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", user.getId());

        // When & Then
        mvc.perform(
                get("/api/users/details")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User 정보 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.email").value("asdf@gmail.com"))
                .andExpect(jsonPath("$.data.mbti").value("ISTP"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.budget").value(99_999_999))
                .andDo(print());
    }

    @DisplayName("[POST] 회원 정보 저장 및 수정 - 정상 호출")
    @Test
    void givenUserAndSession_whenTryingUpdateUserInfo_thenReturnsOK() throws Exception {
        // Given
        User user = User.builder()
                .email("asdf@gmail.com")
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .budget(99_999_999)
                .build();
        userRepository.save(user);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", user.getId());

        UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
                .mbti("ENFJ")
                .gender("FEMALE")
                .budget(12_345_678)
                .build();

        // When & Then
        ResultActions action = mvc.perform(
                post("/api/users/details")
                        .session(session)
                        .content(toJSONString(requestDto))
                        .contentType(APPLICATION_JSON)
        );

        //Then
        action
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User 정보 저장에 성공했습니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andDo(print());

        Optional<User> foundUser = userRepository.findById(user.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).extracting("email", "mbti", "gender", "budget")
                .containsExactly("asdf@gmail.com", ENFJ, FEMALE, 12_345_678);

    }

}