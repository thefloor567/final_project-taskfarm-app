package com.team4.taskfarm.user.domain.stats.dto;

import com.team4.taskfarm.common.entity.category.TbCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryCompletionResponse {

    private String categoryName;
    private long doneCount;
    private long totalCount;
    private int completionRate;
    private String color;

    public static CategoryCompletionResponse from(TbCategory category, long doneCount, long totalCount, int completionRate) {
        return CategoryCompletionResponse.builder()
                .categoryName(category.getName())
                .doneCount(doneCount)
                .totalCount(totalCount)
                .completionRate(completionRate)
                .color(category.getColor())
                .build();
    }
}
