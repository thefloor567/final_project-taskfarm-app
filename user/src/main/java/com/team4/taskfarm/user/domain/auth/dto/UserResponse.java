package com.team4.taskfarm.user.domain.auth.dto;

import com.team4.taskfarm.user.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class UserResponse {
    private Long idxUser;
    private String email;
    private String nickname;
    private int level;
    private int exp;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .idxUser(user.getIdxUser())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .level(user.getLevel())
            .exp(user.getExp())
            .build();
    }
}