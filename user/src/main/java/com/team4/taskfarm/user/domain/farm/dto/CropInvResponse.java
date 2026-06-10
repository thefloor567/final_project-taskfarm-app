package com.team4.taskfarm.user.domain.farm.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 보유 수확 작물 한 줄 (인벤토리 화면).
 */
@Getter
@Builder
public class CropInvResponse {

    private Long seedId;    // 작물 종류 = 씨앗 Idx
    private String name;    // 작물(씨앗) 이름
    private String code;    // 이모지 매핑용 코드
    private int count;      // 보유 수량
}