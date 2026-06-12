package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.TbDailyBuy;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.farm.repository.TbDailyBuyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 하루 구매 한도 검증·기록 (서버 권위, 공용).
 * 씨앗/도구/밭 상점이 공통으로 사용 (tbDailyBuy = SEED/TOOL/PLOT 공용 테이블).
 *
 * 한도값(dailyLimit)은 각 상품(tbSeed/tbTool)이 보유 → 호출측이 넘긴다.
 * (밭은 한도 개념이 약해 보유검증으로 막고, 여기서는 기록만 호출 가능)
 *
 * 사용 패턴 (구매 트랜잭션 안에서):
 *   1) checkAndRecord(farmId, SEED, seedId, qty, seed.getDailyLimit())  // 검증+기록 한 번에
 *      또는
 *   1) ensureWithinLimit(...)  // 검증만
 *   2) record(...)             // 기록만
 */
@Service
@RequiredArgsConstructor
public class DailyBuyService {

    private final TbDailyBuyRepository dailyBuyRepository;
    

    /**
     * 하루 한도 검증 + 구매 기록 (한 번에).
     * 오늘 누적(cnt) + 이번수량 > limit 이면 예외. 통과하면 기록(increase/of).
     *
     * @param dailyLimit 0 이하이면 무제한으로 간주(검증 스킵, 기록만)
     */
    @Transactional
    public void checkAndRecord(Long idxFarm, TbDailyBuy.ItemType type, Long itemIdx,
                               int qty, int dailyLimit) {
        LocalDate today = LocalDate.now();

        TbDailyBuy record = dailyBuyRepository
                .findByIdxFarmAndItemTypeAndItemIdxAndBuyDate(idxFarm, type, itemIdx, today)
                .orElse(null);

        int already = (record == null) ? 0 : record.getCnt();

        // 한도 검증 (limit > 0 일 때만)
        if (dailyLimit > 0 && already + qty > dailyLimit) {
            int remain = Math.max(0, dailyLimit - already);
            throw CustomException.badRequest(
                    "오늘 구매 한도를 초과했어요. (하루 " + dailyLimit + "개, 오늘 남은 수량 " + remain + "개)");
        }

        // 기록 (있으면 cnt 증가, 없으면 신규)
        if (record == null) {
            dailyBuyRepository.save(TbDailyBuy.of(idxFarm, type, itemIdx, today, qty));
        } else {
            record.increase(qty);
        }
    }
}