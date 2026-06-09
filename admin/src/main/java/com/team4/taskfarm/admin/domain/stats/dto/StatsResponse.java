package com.team4.taskfarm.admin.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalTodos;
    private double avgCompletion;

    private List<String> signupLabels;
    private List<Long> signupData;

    private List<String> levelLabels;
    private List<Long> levelData;

    private List<String> categoryLabels;
    private List<Long> categoryData;

    private List<String> weekdayLabels;
    private List<Long> weekdayData;
}
