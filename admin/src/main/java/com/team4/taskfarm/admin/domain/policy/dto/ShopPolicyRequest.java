package com.team4.taskfarm.admin.domain.policy.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ShopPolicyRequest {
    @Min(0) private int price;
    @Min(0) private int reward;   // 씨앗=수확량 보상, 도구=효과수치(uses)
    @Min(0) private int stock;
    @Min(0) private int dailyLimit;
}
