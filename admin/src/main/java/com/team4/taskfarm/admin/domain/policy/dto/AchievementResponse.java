package com.team4.taskfarm.admin.domain.policy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team4.taskfarm.common.entity.social.TbAchievement;
import lombok.Getter;

@Getter
public class AchievementResponse {
    private final Long id;
    private final String code;
    private final String name;
    private final String title;
    private final String category;
    private final String condType;
    private final int condValue;
    private final int rewardCoin;
    @JsonProperty("isActive")
    private final boolean isActive;

    public AchievementResponse(TbAchievement a) {
        this.id = a.getIdxAchievement();
        this.code = a.getCode();
        this.name = a.getName();
        this.title = a.getTitle();
        this.category = a.getCategory().name();
        this.condType = a.getCondType();
        this.condValue = a.getCondValue();
        this.rewardCoin = a.getRewardCoin();
        this.isActive = a.isActive();
    }
}
