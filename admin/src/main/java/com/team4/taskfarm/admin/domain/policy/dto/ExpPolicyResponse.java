package com.team4.taskfarm.admin.domain.policy.dto;

import com.team4.taskfarm.common.entity.exp.TbExpPolicy;
import lombok.Getter;

@Getter
public class ExpPolicyResponse {
    private final Long id;
    private final String priority;
    private final int baseExp;
    private final int doneDrops;
    private final int levelUpDrops;

    public ExpPolicyResponse(TbExpPolicy p) {
        this.id = p.getIdxExpPolicy();
        this.priority = p.getPriority().name();
        this.baseExp = p.getBaseExp();
        this.doneDrops = p.getDoneDrops();
        this.levelUpDrops = p.getLevelUpDrops();
    }
}
