package com.team4.taskfarm.admin.domain.policy.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventConfigRequest {
    @Min(value = 0, message = "weight : 0 이상이어야 합니다") private int weight;
    private boolean active;
}
