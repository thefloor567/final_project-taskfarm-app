package com.team4.taskfarm.user.domain.friend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendRequestDto {

    @NotBlank(message = "친구코드를 입력해주세요.")
    private String code;
}