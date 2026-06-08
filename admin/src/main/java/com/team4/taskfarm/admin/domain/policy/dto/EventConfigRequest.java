package com.team4.taskfarm.admin.domain.policy.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class EventConfigRequest {
    @Min(0) private int weight;
    private boolean active;
}
