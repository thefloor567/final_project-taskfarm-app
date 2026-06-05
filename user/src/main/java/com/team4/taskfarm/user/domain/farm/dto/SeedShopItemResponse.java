package com.team4.taskfarm.user.domain.farm.dto;

import com.team4.taskfarm.common.entity.farm.TbSeed;
import lombok.Builder;
import lombok.Getter;

/**
 * 상점 진열 씨앗 한 줄.
 */
@Getter
@Builder
public class SeedShopItemResponse {

    private Long seedId;
    private String code;
    private String name;
    private int price;      // 구매 가격(코인)
    private int reward;     // 판매 보상(코인)
    private int waters;     // 물주기 필요 횟수
    private int stock;      // 남은 재고
    private int dailyLimit; // 하루 구매 한도(현재 표시만, 검증은 다음 작업)

    public static SeedShopItemResponse of(TbSeed s) {
        return SeedShopItemResponse.builder()
                .seedId(s.getIdxSeed())
                .code(s.getCode())
                .name(s.getName())
                .price(s.getPrice())
                .reward(s.getReward())
                .waters(s.getWaters())
                .stock(s.getStock())
                .dailyLimit(s.getDailyLimit())
                .build();
    }
}