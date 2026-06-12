package com.team4.taskfarm.user.domain.friend.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendPageResponseDto {

    private String myCode;
    private int pendingCount;
    private List<FriendResponseDto> friends;
    private List<FriendResponseDto> received;
    private List<FriendResponseDto> sent;
}