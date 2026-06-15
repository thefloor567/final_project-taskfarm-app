package com.team4.taskfarm.user.domain.achievement.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AchievementItemResponse {
    private String code;
    private String name;
    private String title;        // 칭호 (이모지 포함)
    private String category;     // todo/farm/event/ai/social
    private int condValue;       // 목표치
    private int progress;        // 현재값 (achieved면 condValue로 캡)
    private boolean achieved;
    private int rewardCoin;
}