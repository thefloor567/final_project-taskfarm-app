package com.team4.taskfarm.admin.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {
    private long totalUsers;
    private long todayUsers;
    private long totalTodos;
    private long completedTodos;
    private double completionRate;
    private List<String> signupLabels;  // 날짜 라벨 ["06-01", ...]
    private List<Long> signupData;      // 날짜별 가입자 수
}
