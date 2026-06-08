package com.team4.taskfarm.admin.domain.policy.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ExpPolicyRequest {
    @Min(0) private int baseExp;
    @Min(0) private int doneDrops;
    @Min(0) private int levelUpDrops;
}
