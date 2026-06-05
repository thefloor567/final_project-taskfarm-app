package com.team4.taskfarm.user.domain.farm.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 주민 주문 한 건 (슬롯). 요구 작물 + 보상 + 이행 가능 여부.
 */
@Getter
@Builder
public class OrderResponse {

    private Long orderId;
    private String villager;
    private int reward;          // 보상 코인
    private String state;        // OPEN / DONE
    private boolean canFulfill;  // 현재 보유 작물로 이행 가능한지 (서버 판단)
    private List<OrderItemDto> items;

    @Getter
    @Builder
    public static class OrderItemDto {
        private Long seedId;
        private String cropName; // 작물(씨앗) 이름
        private String code;     // 이모지 매핑용 코드
        private int need;        // 요구 수량
        private int have;        // 현재 보유 수량
    }
}