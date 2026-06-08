package com.team4.taskfarm.admin.domain.dashboard.service;

import com.team4.taskfarm.admin.domain.auth.repository.AdminUserRepository;
import com.team4.taskfarm.admin.domain.dashboard.dto.DashboardResponse;
import com.team4.taskfarm.admin.domain.dashboard.repository.AdminTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AdminUserRepository userRepository;
    private final AdminTodoRepository todoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getAdminHome() {
        LocalDate today = LocalDate.now();
        long totalUsers = userRepository.count();
        long todayUsers = userRepository.countTodaySignups(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        long totalTodos = todoRepository.count();
        long completedTodos = todoRepository.countByIsDoneTrue();
        double rate = totalTodos > 0
                ? Math.round((double) completedTodos / totalTodos * 1000.0) / 10.0
                : 0.0;

        // 최근 7일 가입자 추이
        List<Object[]> rows = userRepository.countSignupsLast7Days();
        Map<String, Long> countByDate = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (Object[] row : rows) {
            String date = row[0].toString().substring(5, 10); // "2026-06-05" → "06-05"
            countByDate.put(date, ((Number) row[1]).longValue());
        }

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String label = today.minusDays(i).format(formatter);
            labels.add(label);
            data.add(countByDate.getOrDefault(label, 0L));
        }

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .todayUsers(todayUsers)
                .totalTodos(totalTodos)
                .completedTodos(completedTodos)
                .completionRate(rate)
                .signupLabels(labels)
                .signupData(data)
                .build();
    }
}
