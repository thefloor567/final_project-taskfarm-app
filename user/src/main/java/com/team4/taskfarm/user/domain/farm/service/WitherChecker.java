package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.TbCrop;
import com.team4.taskfarm.common.entity.farm.TbFarm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 시듦(wither) 판정을 한 곳에 모은 컴포넌트 (lazy 판정).
 *
 * 작물을 "읽는" 모든 지점(getFarm, 주문 조회 등)에서 이걸 거쳐야
 * 화면마다 작물 상태가 달라지지 않는다.
 *
 * 동작:
 *  - growing 작물이 기준 시간(witherHours)을 넘겼으면 시들 차례
 *  - 농장에 허수아비(ScarecrowLeft)가 있으면 방어 발동(시듦 막고 타이머 리셋, 허수아비 -1)
 *  - 없으면 withered 전환
 *
 * 기준 시간은 설정값(application.yml, 분 단위) → 테스트 2분 / 운영 1440분(24h).
 */
@Component
public class WitherChecker {

    /**
     * 시듦 기준 시간(분 단위). 미설정 시 1440분(=24h).
     * 테스트/시연 시 application.yml 에서 짧게(예: 2) 두면 2분 뒤 시듦.
     */
    @Value("${farm.wither-minutes:1440}")
    private long witherMinutes;

    /**
     * 작물 목록에 대해 시듦 판정 적용. 상태가 바뀐 작물이 있으면 호출측에서 저장.
     * 허수아비 방어가 발동하면 farm.scarecrowLeft 가 차감된다(같은 트랜잭션 내 dirty checking).
     *
     * @return 상태가 변경된 작물이 하나라도 있으면 true
     */
    public boolean applyWither(TbFarm farm, List<TbCrop> crops) {
        boolean changed = false;
        for (TbCrop crop : crops) {
            if (!crop.isWitherDue(witherMinutes)) continue;

            // 시들 차례 — 허수아비 있으면 방어
            if (farm.useScarecrow()) {
                crop.protectFromWither();   // 타이머 리셋, 시들지 않음
            } else {
                crop.wither();
            }
            changed = true;
        }
        return changed;
    }

    /** 단건 판정 (필요 시) */
    public boolean applyWither(TbFarm farm, TbCrop crop) {
        return applyWither(farm, List.of(crop));
    }

    public long getWitherMinutes() {
        return witherMinutes;
    }
}