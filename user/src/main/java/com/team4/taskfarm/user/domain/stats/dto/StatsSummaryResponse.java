package com.team4.taskfarm.user.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsSummaryResponse {

    private long totalDone;
    private long totalTodo;
    private int completionRate;
    private int currentLevel;
    private int currentExp;

    public static StatsSummaryResponse of(long totalDone, long totalTodo, int completionRate, int currentLevel, int currentExp) {
        return StatsSummaryResponse.builder()
                .totalDone(totalDone)
                .totalTodo(totalTodo)
                .completionRate(completionRate)
                .currentLevel(currentLevel)
                .currentExp(currentExp)
                .build();
    }
}
