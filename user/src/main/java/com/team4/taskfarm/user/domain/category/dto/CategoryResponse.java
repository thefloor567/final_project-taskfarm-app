package com.team4.taskfarm.user.domain.category.dto;

import com.team4.taskfarm.common.entity.category.TbCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long idxCat;
    private String name;
    private String color;

    public static CategoryResponse from(TbCategory category) {
        return CategoryResponse.builder()
            .idxCat(category.getIdxCat())
            .name(category.getName())
            .color(category.getColor())
            .build();
    }
}