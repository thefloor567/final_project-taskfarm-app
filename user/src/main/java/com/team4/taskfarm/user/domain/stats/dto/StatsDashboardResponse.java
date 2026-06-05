package com.team4.taskfarm.user.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StatsDashboardResponse {

    private StatsSummaryResponse summary;
    private List<CategoryCompletionResponse> categories;
    private List<WeeklyTodoStatsResponse> weekly;
    private List<ExpGrowthResponse> expGrowth;

    public static StatsDashboardResponse of(
            StatsSummaryResponse summary,
            List<CategoryCompletionResponse> categories,
            List<WeeklyTodoStatsResponse> weekly,
            List<ExpGrowthResponse> expGrowth) {
        return StatsDashboardResponse.builder()
                .summary(summary)
                .categories(categories)
                .weekly(weekly)
                .expGrowth(expGrowth)
                .build();
    }
}
