package com.example.spinlog.user.service;

import com.example.spinlog.global.security.utils.SecurityUtils;
import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.dto.response.ViewUserResponseDto;
import com.example.spinlog.user.entity.Budget;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.event.UserUpdatedEvent;
import com.example.spinlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize("authentication")
    public ViewUserResponseDto findUser() {
        String authenticationName = SecurityUtils.getAuthenticationName();

        User foundUser = getUser(authenticationName);
        Integer budget = foundUser.getCurrentMonthBudget().getBudget();

        return ViewUserResponseDto.of(foundUser, budget);
    }

    @PreAuthorize("authentication")
    public void updateUserInfo(UpdateUserRequestDto requestDto) {
        String authenticationName = SecurityUtils.getAuthenticationName();

        User foundUser = getUser(authenticationName);
        Budget budget = foundUser.getCurrentMonthBudget();

        User originalUser = foundUser.copyEntity();

        foundUser.change(requestDto.getMbti(), requestDto.getGender());
        budget.change(requestDto.getBudget());

        eventPublisher.publishEvent(new UserUpdatedEvent(originalUser, foundUser));
    }

    private User getUser(String authenticationName) {
        return userRepository.findByAuthenticationName(authenticationName).orElseThrow(() ->
                new NoSuchElementException(authenticationName + "에 해당하는 사용자를 찾을 수 없습니다.")
        );
    }

}
