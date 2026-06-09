package com.team4.taskfarm.admin.domain.user.dto;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class UserListResponse {
    private final Long userId;
    private final String nickname;
    private final String email;
    private final int level;
    private final long doneCount;
    private final String joinDate;   // yyyy-MM-dd

    public UserListResponse(UserListRow r) {
        this.userId = r.getUserId();
        this.nickname = r.getNickname();
        this.email = r.getEmail();
        this.level = r.getLevel();
        this.doneCount = r.getDoneCount();
        this.joinDate = r.getJoinDate() != null
                ? r.getJoinDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "-";
    }
}
