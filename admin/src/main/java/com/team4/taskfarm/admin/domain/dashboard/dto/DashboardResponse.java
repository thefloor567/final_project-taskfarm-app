package com.team4.taskfarm.admin.domain.dashboard.dto;
 
import lombok.Builder;
import lombok.Getter;
 
@Getter
@Builder
public class DashboardResponse {
    private long totalUsers;
    private long todayUsers;
    private long totalTodos;
    private long completedTodos;
    private double completionRate;   // 0.0 ~ 100.0
}
 