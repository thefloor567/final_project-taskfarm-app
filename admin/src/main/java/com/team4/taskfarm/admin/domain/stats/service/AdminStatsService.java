package com.team4.taskfarm.admin.domain.stats.service;

import com.team4.taskfarm.admin.domain.stats.dto.StatsResponse;
import com.team4.taskfarm.admin.domain.stats.repository.AdminStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AdminStatsRepository statsRepository;

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        long totalUsers = statsRepository.countTotalUsers();
        long activeUsers = statsRepository.countActiveUsers(LocalDateTime.now().minusDays(7));
        long totalTodos = statsRepository.countTotalTodos();
        long doneTodos = statsRepository.countDoneTodos();
        double avgCompletion = totalTodos > 0
                ? Math.round((double) doneTodos / totalTodos * 1000.0) / 10.0
                : 0.0;

        // 월별 가입자 (최근 6개월, 빈 달 0)
        Map<String, Long> signupMap = new HashMap<>();
        for (Object[] r : statsRepository.signupByMonth()) {
            signupMap.put(r[0].toString(), ((Number) r[1]).longValue());
        }
        List<String> signupLabels = new ArrayList<>();
        List<Long> signupData = new ArrayList<>();
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate base = LocalDate.now().withDayOfMonth(1);
        for (int i = 5; i >= 0; i--) {
            LocalDate m = base.minusMonths(i);
            String key = m.format(ymFmt);
            signupLabels.add((m.getMonthValue()) + "월");
            signupData.add(signupMap.getOrDefault(key, 0L));
        }

        // 레벨 분포
        List<String> levelLabels = Arrays.asList("Lv1-3", "Lv4-6", "Lv7-9", "Lv10+");
        List<Long> levelData = new ArrayList<>();
        List<Object[]> lvRows = statsRepository.levelDistribution();
        if (!lvRows.isEmpty()) {
            Object[] g = lvRows.get(0);
            for (int i = 0; i < 4; i++) {
                levelData.add(g[i] == null ? 0L : ((Number) g[i]).longValue());
            }
        } else {
            levelData.addAll(Arrays.asList(0L, 0L, 0L, 0L));
        }

        // 카테고리별 할일
        List<String> categoryLabels = new ArrayList<>();
        List<Long> categoryData = new ArrayList<>();
        for (Object[] r : statsRepository.todoByCategory()) {
            categoryLabels.add(r[0].toString());
            categoryData.add(((Number) r[1]).longValue());
        }

        // 요일별 완료 (월~일 순서로 재배치)
        Map<Integer, Long> dowMap = new HashMap<>();
        for (Object[] r : statsRepository.completionByWeekday()) {
            dowMap.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }
        // MySQL DAYOFWEEK: 1=일,2=월,...,7=토 → 화면 월~일 = 2,3,4,5,6,7,1
        List<String> weekdayLabels = Arrays.asList("월", "화", "수", "목", "금", "토", "일");
        int[] dowOrder = {2, 3, 4, 5, 6, 7, 1};
        List<Long> weekdayData = new ArrayList<>();
        for (int dow : dowOrder) {
            weekdayData.add(dowMap.getOrDefault(dow, 0L));
        }

        return StatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalTodos(totalTodos)
                .avgCompletion(avgCompletion)
                .signupLabels(signupLabels)
                .signupData(signupData)
                .levelLabels(levelLabels)
                .levelData(levelData)
                .categoryLabels(categoryLabels)
                .categoryData(categoryData)
                .weekdayLabels(weekdayLabels)
                .weekdayData(weekdayData)
                .build();
    }
}
