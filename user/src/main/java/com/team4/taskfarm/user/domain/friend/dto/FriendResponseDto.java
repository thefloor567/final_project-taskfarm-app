package com.team4.taskfarm.user.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendResponseDto {

    private Long id;
    private String nickname;
    private String code;
    private Integer level;
    private Integer rank;
    private String title;
}