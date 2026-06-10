package com.team4.taskfarm.user.domain.farm.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 보유 씨앗 한 줄 (심을 씨앗 고르기 UI).
 */
@Getter
@Builder
public class SeedInvResponse {

    private Long seedId;
    private String name;   // 씨앗(작물) 이름
    private int waters;    // 물주기 필요 횟수
    private int qty;       // 보유 수량
    private String code;
}