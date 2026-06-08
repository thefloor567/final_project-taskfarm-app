package com.team4.taskfarm.admin.domain.policy.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedPolicyRequest {
    @Min(value = 0, message = "price : 0 이상이어야 합니다") private int price;
    @Min(value = 0, message = "reward : 0 이상이어야 합니다") private int reward;
    @Min(value = 0, message = "stock : 0 이상이어야 합니다") private int stock;
    @Min(value = 0, message = "dailyLimit : 0 이상이어야 합니다") private int dailyLimit;
}
