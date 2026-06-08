package com.team4.taskfarm.admin.domain.policy.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpPolicyRequest {
    @Min(value = 0, message = "baseExp : 0 이상이어야 합니다") private int baseExp;
    @Min(value = 0, message = "doneDrops : 0 이상이어야 합니다") private int doneDrops;
    @Min(value = 0, message = "levelUpDrops : 0 이상이어야 합니다") private int levelUpDrops;
}
