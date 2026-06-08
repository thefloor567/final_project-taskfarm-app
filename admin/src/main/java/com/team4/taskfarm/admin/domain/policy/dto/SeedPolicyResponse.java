package com.team4.taskfarm.admin.domain.policy.dto;

import com.team4.taskfarm.common.entity.farm.TbSeed;
import lombok.Getter;

@Getter
public class SeedPolicyResponse {
    private final Long id;
    private final String code;
    private final String name;
    private final int price;
    private final int reward;
    private final int stock;
    private final int dailyLimit;
    private final boolean active;

    public SeedPolicyResponse(TbSeed s) {
        this.id = s.getIdxSeed();
        this.code = s.getCode();
        this.name = s.getName();
        this.price = s.getPrice();
        this.reward = s.getReward();
        this.stock = s.getStock();
        this.dailyLimit = s.getDailyLimit();
        this.active = s.isActive();
    }
}
