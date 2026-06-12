package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.dto.PlotShopItemResponse;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 밭 판매 (서버 권위). 기본 6밭 무료, 7번째부터 레벨+코인 해금.
 * 설계서 team4-farm-expansion-design §4 / 인수인계서 §4 반영.
 *
 * 구매는 tbPlot row insert 만으로 끝난다(테이블 스키마 변경 X).
 * getFarm 은 tbPlot row 수로 밭을 합산하므로 자동 반영.
 */
@Service
@RequiredArgsConstructor
public class PlotShopService {

    private final TbFarmRepository farmRepository;
    private final AuthUserRepository userRepository;
    private final TbPlotRepository plotRepository;
    private final TbPlotPriceRepository plotPriceRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;
    private final TbDailyBuyRepository dailyBuyRepository;
    private final DailyBuyService dailyBuyService;

    /** 밭 상점 진열 (보유/잠김 표시) */
    @Transactional(readOnly = true)
    public List<PlotShopItemResponse> getPlotShop(Long idxUser) {
        TbFarm farm = getFarm(idxUser);
        int level = currentLevel(idxUser);

        // 이미 보유한 슬롯 집합
        Set<Integer> ownedSlots = plotRepository.findByIdxFarmOrderBySlotAsc(farm.getIdxFarm())
                .stream().map(TbPlot::getSlot).collect(Collectors.toSet());

        return plotPriceRepository.findAllByOrderBySlotAsc().stream()
                .map(p -> PlotShopItemResponse.builder()
                        .slot(p.getSlot())
                        .unlockLevel(p.getUnlockLevel())
                        .price(p.getPrice())
                        .owned(ownedSlots.contains(p.getSlot()))
                        .locked(level < p.getUnlockLevel())
                        .build())
                .toList();
    }

    /** 밭 구매 */
    @Transactional
    public void buyPlot(Long idxUser, int slot) {
        TbFarm farm = getFarm(idxUser);

        // [1] 가격표 조회
        TbPlotPrice price = plotPriceRepository.findBySlot(slot)
                .orElseThrow(() -> CustomException.badRequest("구매할 수 없는 밭입니다."));

        // [2] 검증: 이미 보유?
        boolean alreadyOwned = plotRepository.findByIdxFarmOrderBySlotAsc(farm.getIdxFarm())
                .stream().anyMatch(p -> p.getSlot() == slot);
        if (alreadyOwned) {
            throw CustomException.badRequest("이미 보유한 밭입니다.");
        }
        // 레벨 해금
        if (currentLevel(idxUser) < price.getUnlockLevel()) {
            throw CustomException.badRequest("Lv " + price.getUnlockLevel() + " 부터 구매할 수 있는 밭입니다.");
        }

        // [3] tbPlot row insert (slot 부여)
        plotRepository.save(TbPlot.create(farm.getIdxFarm(), slot));

        // [4] 코인 차감 + 원장 기록
        farm.spendCoin(price.getPrice());
        coinLedgerRepository.save(
                TbCoinLedger.spend(farm.getIdxFarm(), price.getPrice(), "BUY_PLOT", (long) slot));

        // 밭은 한도 검증 불필요(보유검증으로 막힘) → limit 0 으로 기록만
        dailyBuyService.checkAndRecord(
                farm.getIdxFarm(), TbDailyBuy.ItemType.PLOT, (long) slot, 1, 0);
    }

    // ── 공통 ──

    private TbFarm getFarm(Long idxUser) {
        return farmRepository.findByIdxUser(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));
    }

    private int currentLevel(Long idxUser) {
        return userRepository.findById(idxUser).map(TbUser::getLevel).orElse(1);
    }
}