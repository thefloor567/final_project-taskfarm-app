package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.dto.SeedShopItemResponse;
import com.team4.taskfarm.user.domain.farm.policy.SeedUnlockPolicy;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 씨앗 상점 (서버 권위).
 * 구매는 "판매중·재고 → 레벨 → 코인 → (차감 · 원장기록 · 재고감소 · 씨앗적립)"을 한 트랜잭션으로 처리.
 * coin 캐시값과 코인원장(ledger)이 절대 어긋나지 않도록 같은 트랜잭션에 묶는다.
 */
@Service
@RequiredArgsConstructor
public class SeedShopService {

    private final TbFarmRepository farmRepository;
    private final TbSeedRepository seedRepository;
    private final TbSeedInvRepository seedInvRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;
    private final AuthUserRepository userRepository;
    private final DailyBuyService dailyBuyService;

    /** 상점 진열 목록 (판매중인 씨앗) */
    @Transactional(readOnly = true)
    public List<SeedShopItemResponse> getShopItems() {
        return seedRepository.findByIsActiveTrueOrderByIdxSeedAsc().stream()
                .map(SeedShopItemResponse::of)
                .toList();
    }

    /**
     * 씨앗 구매.
     * @param idxUser 구매자
     * @param seedId  씨앗
     * @param qty     수량(1 이상)
     */
    @Transactional
    public void buySeed(Long idxUser, Long seedId, int qty) {
        TbFarm farm = farmRepository.findByIdxUserForUpdate(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));

        TbSeed seed = seedRepository.findByIdForUpdate(seedId)
                .orElseThrow(() -> CustomException.notFound("씨앗 정보를 찾을 수 없습니다."));

        // ⓪ 해금 레벨 검증 (유저 레벨이 씨앗 해금 레벨 미만이면 구매 차단)
        //    주문(OrderService)과 동일한 SeedUnlockPolicy를 사용해 정책 일관성 보장.
        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));
        if (!SeedUnlockPolicy.isUnlocked(seed.getCode(), user.getLevel())) {
            throw CustomException.badRequest(
                    "레벨 " + SeedUnlockPolicy.levelOf(seed.getCode())
                    + "부터 구매할 수 있는 씨앗입니다. (현재 레벨 " + user.getLevel() + ")");
        }

        int totalPrice = seed.getPrice() * qty;

        // ① 판매중·재고 검증 + 재고 차감 (엔티티가 검증)
        seed.purchase(qty);

        // ② 코인 차감 (부족하면 예외 → 트랜잭션 롤백)
        farm.spendCoin(totalPrice);

        // ③ 코인원장 SPEND 기록 (coin 캐시값과 한 트랜잭션)
        coinLedgerRepository.save(
                TbCoinLedger.spend(farm.getIdxFarm(), totalPrice,
                        "씨앗 구매: " + seed.getName() + " x" + qty, seed.getIdxSeed()));

        // ④ 보유 씨앗 적립 (있으면 +, 없으면 신규)
        seedInvRepository.findByIdxFarmAndIdxSeed(farm.getIdxFarm(), seedId)
                .ifPresentOrElse(
                        inv -> inv.add(qty),
                        () -> seedInvRepository.save(
                                TbSeedInv.create(farm.getIdxFarm(), seedId, qty))
                );

        // ⑤ 하루한도 검증 + 기록 (SEED)
        dailyBuyService.checkAndRecord(
                farm.getIdxFarm(), TbDailyBuy.ItemType.SEED, seedId, qty, seed.getDailyLimit());
    }
}