package com.team4.taskfarm.user.domain.stats.service;

import com.team4.taskfarm.common.entity.category.TbCategory;
import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.user.domain.stats.dto.CategoryCompletionResponse;
import com.team4.taskfarm.user.domain.stats.dto.ExpGrowthResponse;
import com.team4.taskfarm.user.domain.stats.dto.StatsDashboardResponse;
import com.team4.taskfarm.user.domain.stats.dto.StatsSummaryResponse;
import com.team4.taskfarm.user.domain.stats.dto.WeeklyTodoStatsResponse;
import com.team4.taskfarm.user.domain.stats.repository.StatsCategoryRepository;
import com.team4.taskfarm.user.domain.stats.repository.StatsExpLedgerRepository;
import com.team4.taskfarm.user.domain.stats.repository.StatsTodoRepository;
import com.team4.taskfarm.user.domain.stats.repository.StatsUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final String[] DAY_LABELS = {"월", "화", "수", "목", "금", "토", "일"};

    private final StatsTodoRepository statsTodoRepository;
    private final StatsCategoryRepository statsCategoryRepository;
    private final StatsUserRepository statsUserRepository;
    private final StatsExpLedgerRepository statsExpLedgerRepository;

    @Transactional(readOnly = true)
    public StatsDashboardResponse getDashboard(Long idxUser) {
        return StatsDashboardResponse.of(
                getSummary(idxUser),
                getCategoryCompletions(idxUser),
                getWeeklyTodoStats(idxUser),
                getExpGrowth(idxUser));
    }

    private StatsSummaryResponse getSummary(Long idxUser) {
        long totalTodo = statsTodoRepository.countByIdxUserAndDeleteDateIsNull(idxUser);
        long totalDone = statsTodoRepository.countByIdxUserAndIsDoneTrueAndDeleteDateIsNull(idxUser);
        TbUser user = statsUserRepository.findByIdxUserAndDeleteDateIsNull(idxUser).orElse(null);

        return StatsSummaryResponse.of(
                totalDone,
                totalTodo,
                toRate(totalDone, totalTodo),
                user == null ? 1 : user.getLevel(),
                user == null ? 0 : user.getExp());
    }

    private List<CategoryCompletionResponse> getCategoryCompletions(Long idxUser) {
        List<TbCategory> categories = statsCategoryRepository.findByIdxUserAndDeleteDateIsNullOrderByCreateDateAsc(idxUser);
        List<CategoryCompletionResponse> responses = new ArrayList<>();

        for (TbCategory category : categories) {
            long totalCount = statsTodoRepository.countByIdxUserAndIdxCatAndDeleteDateIsNull(idxUser, category.getIdxCat());
            long doneCount = statsTodoRepository.countByIdxUserAndIdxCatAndIsDoneTrueAndDeleteDateIsNull(idxUser, category.getIdxCat());

            responses.add(CategoryCompletionResponse.from(category, doneCount, totalCount, toRate(doneCount, totalCount)));
        }

        return responses;
    }

    private List<WeeklyTodoStatsResponse> getWeeklyTodoStats(Long idxUser) {
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();
        Map<LocalDate, Long> doneCountByDate = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            doneCountByDate.put(startDate.plusDays(i), 0L);
        }

        List<TbTodo> completedTodos = statsTodoRepository.findByIdxUserAndIsDoneTrueAndCompleteDateBetweenAndDeleteDateIsNull(
                idxUser, startDate.atStartOfDay(), LocalDateTime.of(endDate, LocalTime.MAX));

        for (TbTodo todo : completedTodos) {
            if (todo.getCompleteDate() == null) continue;
            LocalDate completeDate = todo.getCompleteDate().toLocalDate();
            doneCountByDate.computeIfPresent(completeDate, (date, count) -> count + 1);
        }

        return doneCountByDate.entrySet().stream()
                .map(entry -> WeeklyTodoStatsResponse.of(toDayLabel(entry.getKey()), entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<ExpGrowthResponse> getExpGrowth(Long idxUser) {
        LocalDate startWeek = LocalDate.now()
                .minusWeeks(4)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = LocalDate.now();
        int[] weeklyAmounts = new int[5];

        List<TbExpLedger> ledgers = statsExpLedgerRepository.findByIdxUserAndTypeAndCreateDateBetweenOrderByCreateDateAsc(
                idxUser,
                TbExpLedger.LedgerType.EARN,
                startWeek.atStartOfDay(),
                LocalDateTime.of(endDate, LocalTime.MAX));

        for (TbExpLedger ledger : ledgers) {
            if (ledger.getCreateDate() == null) continue;
            LocalDate ledgerWeek = ledger.getCreateDate().toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            long weekIndex = ChronoUnit.WEEKS.between(startWeek, ledgerWeek);
            if (weekIndex >= 0 && weekIndex < weeklyAmounts.length) {
                weeklyAmounts[(int) weekIndex] += ledger.getAmount();
            }
        }

        List<ExpGrowthResponse> responses = new ArrayList<>();
        int cumulativeAmount = 0;
        for (int i = 0; i < weeklyAmounts.length; i++) {
            cumulativeAmount += weeklyAmounts[i];
            responses.add(ExpGrowthResponse.of((i + 1) + "주", cumulativeAmount));
        }

        return responses;
    }

    private int toRate(long value, long total) {
        if (total == 0) return 0;
        return (int) Math.round((double) value * 100 / total);
    }

    private String toDayLabel(LocalDate date) {
        return DAY_LABELS[date.getDayOfWeek().getValue() - 1];
    }
}
