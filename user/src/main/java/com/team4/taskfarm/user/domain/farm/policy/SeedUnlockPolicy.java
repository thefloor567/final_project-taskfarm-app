package com.team4.taskfarm.user.domain.farm.policy;

import java.util.Map;

/**
 * 씨앗 해금 레벨 정책.
 *
 * tbSeed에 해금 레벨 컬럼이 없어 코드 상수로 관리한다.
 * (이전엔 OrderService / SeedShopService에 같은 맵이 중복되어 있어,
 *  한쪽만 고치면 정책이 어긋날 위험이 있었음 → 이 클래스로 일원화)
 *
 * 추후 tbSeed.unlockLevel 컬럼 또는 어드민 정책 테이블로 이관할 경우,
 * 호출부는 그대로 두고 이 클래스 내부 구현만 바꾸면 된다.
 */
public final class SeedUnlockPolicy {

    private SeedUnlockPolicy() {}

    /** 씨앗 code → 해금 레벨. 레벨디자인 표4 기준. */
    private static final Map<String, Integer> UNLOCK = Map.of(
            "radish", 1,
            "tomato", 1,
            "corn", 3,
            "pumpkin", 5
    );

    /** 씨앗 code의 해금 레벨. 미등록 code는 제한 없음(1)으로 간주. */
    public static int levelOf(String code) {
        return UNLOCK.getOrDefault(code, 1);
    }

    /** 유저 레벨이 해당 씨앗을 해금했는지 여부. */
    public static boolean isUnlocked(String code, int userLevel) {
        return userLevel >= levelOf(code);
    }
}