package com.team4.taskfarm.user.domain.farm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 씨앗 구매 요청.
 */
@Getter
@NoArgsConstructor
public class BuySeedRequest {

    @NotNull(message = "구매할 씨앗을 선택해 주세요.")
    private Long seedId;

    @Min(value = 1, message = "1개 이상 구매할 수 있습니다.")
    private int qty = 1;
}