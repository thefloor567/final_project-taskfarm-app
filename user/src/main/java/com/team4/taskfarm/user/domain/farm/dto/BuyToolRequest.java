package com.team4.taskfarm.user.domain.farm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도구 구매 요청.
 * 비료(fertilizer)는 targetPlotId(대상 밭) 필수. 허수아비/온실은 불필요(null).
 */
@Getter
@NoArgsConstructor
public class BuyToolRequest {
    private Long targetPlotId;   // 비료 전용 — 어느 밭의 작물에 줄지
}