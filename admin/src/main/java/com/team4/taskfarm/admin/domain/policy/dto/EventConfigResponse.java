package com.team4.taskfarm.admin.domain.policy.dto;

import com.team4.taskfarm.common.entity.farm.TbEventConfig;
import lombok.Getter;

@Getter
public class EventConfigResponse {
    private final Long id;
    private final int streakMin;
    private final String eventKey;
    private final int weight;
    private final boolean active;

    public EventConfigResponse(TbEventConfig e) {
        this.id = e.getIdxEventConfig();
        this.streakMin = e.getStreakMin();
        this.eventKey = e.getEventKey();
        this.weight = e.getWeight();
        this.active = e.isActive();
    }
}
