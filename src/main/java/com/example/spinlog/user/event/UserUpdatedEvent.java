package com.example.spinlog.user.event;

import com.example.spinlog.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserUpdatedEvent {
    User originalUser;
    User updatedUser;
}
