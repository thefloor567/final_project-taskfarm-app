package com.team4.taskfarm.admin.domain.dashboard.service;
 
import com.team4.taskfarm.admin.domain.auth.repository.AdminUserRepository;
import com.team4.taskfarm.admin.domain.dashboard.dto.DashboardResponse;
import com.team4.taskfarm.admin.domain.dashboard.repository.AdminTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
public class DashboardService {
 
    private final AdminUserRepository userRepository;
    private final AdminTodoRepository todoRepository;
 
    @Transactional(readOnly = true)
    public DashboardResponse getAdminHome() {
        long totalUsers = userRepository.count();
        long todayUsers = userRepository.countTodaySignups();
        long totalTodos = todoRepository.count();
        long completedTodos = todoRepository.countByIsDoneTrue();
        double rate = totalTodos > 0
                ? Math.round((double) completedTodos / totalTodos * 1000.0) / 10.0
                : 0.0;
 
        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .todayUsers(todayUsers)
                .totalTodos(totalTodos)
                .completedTodos(completedTodos)
                .completionRate(rate)
                .build();
    }
}