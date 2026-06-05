package com.team4.taskfarm.user.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpGrowthResponse {

    private String label;
    private int amount;

    public static ExpGrowthResponse of(String label, int amount) {
        return ExpGrowthResponse.builder()
                .label(label)
                .amount(amount)
                .build();
    }
}
