package com.team4.taskfarm.admin.domain.ops.dto;

import com.team4.taskfarm.common.entity.social.TbMail;

import java.time.LocalDateTime;

/**
 * 우편 발송 이력 projection (refKey별 그룹 집계).
 */
public interface MailHistoryRow {
    String getRefKey();
    String getTitle();
    long getSentCount();
    TbMail.RewardType getRewardType();
    int getRewardCoin();
    String getRewardTitle();
    LocalDateTime getCreateDate();
}
