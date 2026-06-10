package com.team4.taskfarm.user.domain.auth.dto;

import com.team4.taskfarm.common.entity.user.TbUser;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class UserResponse {
    private Long idxUser;
    private String email;
    private String nickname;
    private int level;
    private int exp;

    public static UserResponse from(TbUser user) {
        return UserResponse.builder()
            .idxUser(user.getIdxUser())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .level(user.getLevel())
            .exp(user.getExp())
            .build();
    }
}