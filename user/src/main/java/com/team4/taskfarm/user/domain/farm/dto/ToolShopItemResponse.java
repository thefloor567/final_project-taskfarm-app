package com.team4.taskfarm.user.domain.farm.dto;

import com.team4.taskfarm.common.entity.farm.TbTool;
import lombok.Builder;
import lombok.Getter;

/**
 * 상점 진열 도구 한 줄.
 */
@Getter
@Builder
public class ToolShopItemResponse {

    private Long toolId;
    private String code;
    private String name;
    private String type;       // scarecrow / fertilizer / greenhouse
    private int uses;          // 사용/방어 횟수
    private int price;
    private int stock;
    private int dailyLimit;
    private int unlockLevel;   // 해금 레벨 (코드 상수에서 주입)
    private boolean locked;    // 현재 유저 레벨 기준 잠김 여부

    public static ToolShopItemResponse of(TbTool t, int unlockLevel, boolean locked) {
        return ToolShopItemResponse.builder()
                .toolId(t.getIdxTool())
                .code(t.getCode())
                .name(t.getName())
                .type(t.getType().name())
                .uses(t.getUses())
                .price(t.getPrice())
                .stock(t.getStock())
                .dailyLimit(t.getDailyLimit())
                .unlockLevel(unlockLevel)
                .locked(locked)
                .build();
    }
}