package com.team4.taskfarm.user.domain.achievement.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AchievementListResponse {
    private String equippedTitle;
    private List<AchievementItemResponse> list;
}