package com.team4.taskfarm.user.domain.farm.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 밭 상점 진열 한 줄.
 */
@Getter
@Builder
public class PlotShopItemResponse {

    private int slot;          // 밭 번호 (7, 8, ...)
    private int unlockLevel;   // 해금 레벨
    private int price;         // 코인 가격
    private boolean owned;     // 이미 보유한 밭
    private boolean locked;    // 현재 레벨 < 해금레벨
}