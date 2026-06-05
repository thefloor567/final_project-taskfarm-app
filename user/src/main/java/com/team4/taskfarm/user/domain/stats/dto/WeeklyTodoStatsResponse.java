package com.team4.taskfarm.user.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class WeeklyTodoStatsResponse {

    private String label;
    private LocalDate date;
    private long doneCount;

    public static WeeklyTodoStatsResponse of(String label, LocalDate date, long doneCount) {
        return WeeklyTodoStatsResponse.builder()
                .label(label)
                .date(date)
                .doneCount(doneCount)
                .build();
    }
}
