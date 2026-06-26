package com.team4.taskfarm.admin.domain.user.dto;

import java.time.LocalDateTime;

import com.team4.taskfarm.common.entity.user.TbUser;

/**
 * 유저 목록 조회용 projection (유저 기본정보 + 완료 할일 수).
 */
public interface UserListRow {
    Long getUserId();
    String getNickname();
    String getEmail();
    int getLevel();
    LocalDateTime getJoinDate();
    long getDoneCount();
    TbUser.Status getStatus();
}
