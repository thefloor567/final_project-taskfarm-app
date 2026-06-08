package com.team4.taskfarm.admin.domain.policy.dto;

import com.team4.taskfarm.common.entity.farm.TbTool;
import lombok.Getter;

@Getter
public class ToolPolicyResponse {
    private final Long id;
    private final String code;
    private final String name;
    private final String type;
    private final int price;
    private final int uses;       // 효과수치
    private final int stock;
    private final int dailyLimit;
    private final boolean active;

    public ToolPolicyResponse(TbTool t) {
        this.id = t.getIdxTool();
        this.code = t.getCode();
        this.name = t.getName();
        this.type = t.getType().name();
        this.price = t.getPrice();
        this.uses = t.getUses();
        this.stock = t.getStock();
        this.dailyLimit = t.getDailyLimit();
        this.active = t.isActive();
    }
}
