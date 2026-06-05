package com.team4.taskfarm.user.domain.farm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 씨앗 심기 요청. 어떤 씨앗을 심을지 seedId로 지정.
 */
@Getter
@NoArgsConstructor
public class PlantRequest {

    @NotNull(message = "심을 씨앗을 선택해 주세요.")
    private Long seedId;
}