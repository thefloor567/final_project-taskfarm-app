package com.team4.taskfarm.admin.domain.policy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AchievementRequest {
    @Min(value = 1, message = "목표치는 1 이상이어야 합니다") private int condValue;
    @Min(value = 0, message = "보상 코인은 0 이상이어야 합니다") private int rewardCoin;
    @JsonProperty("isActive") private boolean isActive;
}
