package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.dto.ToolShopItemResponse;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 도구 상점 (서버 권위). 도구 3종: 허수아비/비료/온실.
 * 농장확장: 도구를 "특정 밭(targetPlotId)"에 설치 -> tbPlotEffect.
 *   - scarecrow  : 까마귀 방어. tbPlotEffect(scarecrow, RemainUses=uses) 설치.
 *   - greenhouse : 가뭄/시듦 방어. tbPlotEffect(greenhouse, 당일 만료) 설치.
 *   - fertilizer : 즉시 성장 가속(밭 작물 물주기 단축). effect row 남기지 않음.
 * 기존 tbFarm.ScarecrowLeft(전역)는 deprecated -- 더 이상 안 씀.
 */
@Service
@RequiredArgsConstructor
public class ToolShopService {

    private final TbFarmRepository farmRepository;
    private final AuthUserRepository userRepository;
    private final TbToolRepository toolRepository;
    private final TbCropRepository cropRepository;
    private final TbPlotRepository plotRepository;
    private final TbPlotEffectRepository plotEffectRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;
    private final DailyBuyService dailyBuyService;

    // 기획서 §3 코드 상수
    private static final Map<String, Integer> TOOL_UNLOCK = Map.of(
            "scarecrow", 2, "fertilizer", 3, "greenhouse", 5
    );
    private static final int MAX_FERT_BOOST = 2;   // 비료 1회 최대 단축 물주기(캡)

    /** 상점 진열 목록 (현재 레벨 기준 잠김 표시 포함) */
    @Transactional(readOnly = true)
    public List<ToolShopItemResponse> getToolShopList(Long idxUser) {
        int level = currentLevel(idxUser);
        return toolRepository.findByIsActiveTrueOrderByIdxToolAsc().stream()
                .map(t -> {
                    int unlock = TOOL_UNLOCK.getOrDefault(t.getCode(), 1);
                    boolean locked = level < unlock;
                    return ToolShopItemResponse.of(t, unlock, locked);
                })
                .toList();
    }

    /**
     * 도구 구매 + 밭별 효과. 모든 도구가 대상 밭(targetPlotId) 필수.
     */
    @Transactional
    public void buyTool(Long idxUser, Long toolId, Long targetPlotId) {
        TbFarm farm = farmRepository.findByIdxUser(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));

        TbTool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> CustomException.notFound("도구 정보를 찾을 수 없습니다."));

        // 공통 검증: 해금레벨 + 코인 + 재고
        int unlock = TOOL_UNLOCK.getOrDefault(tool.getCode(), 1);
        if (currentLevel(idxUser) < unlock) {
            throw CustomException.badRequest("Lv " + unlock + " 부터 구매할 수 있는 도구입니다.");
        }

        // 모든 도구가 대상 밭 필수 -> 소유 검증(남의 밭 plotId 조작 차단)
        TbPlot plot = requireMyPlot(farm, targetPlotId);

        tool.purchase();                  // 판매중·재고 검증 + 재고 차감
        farm.spendCoin(tool.getPrice());  // 코인 차감(부족 시 예외)
        
        // 하루한도 검증 + 기록 (TOOL). 도구는 1개씩 구매.
        dailyBuyService.checkAndRecord(
                farm.getIdxFarm(), TbDailyBuy.ItemType.TOOL, tool.getIdxTool(), 1, tool.getDailyLimit());

        // Type별 효과 (밭별 설치)
        switch (tool.getType()) {
            case scarecrow  -> applyScarecrow(plot, tool);
            case greenhouse -> applyGreenhouse(plot);
            case fertilizer -> applyFertilizer(plot);
        }

        // 코인 원장 SPEND 기록
        coinLedgerRepository.save(
                TbCoinLedger.spend(farm.getIdxFarm(), tool.getPrice(),
                        "BUY_" + tool.getType().name().toUpperCase(), tool.getIdxTool()));
    }

    // Type별 효과 (밭별)

    /** 허수아비: 그 밭에 까마귀 방어 횟수 비축 (effect 설치) */
    private void applyScarecrow(TbPlot plot, TbTool tool) {
        // 재구매 시 기존 effect 횟수 합산 메서드가 없으므로, 새 effect row 추가.
        // (같은 밭에 허수아비 effect 여러 개여도 까마귀 핸들러가 하나씩 소모 -> 문제없음)
        plotEffectRepository.save(TbPlotEffect.scarecrow(plot.getIdxPlot(), tool.getUses()));
    }

    /** 온실: 그 밭에 당일 가뭄/시듦 방어 설치 */
    private void applyGreenhouse(TbPlot plot) {
        LocalDate today = LocalDate.now();
        // 같은 밭에 온실이 이미 있으면 중복 설치 무의미 -> 없을 때만 설치
        plotEffectRepository
                .findByIdxPlotAndEffectType(plot.getIdxPlot(), TbPlotEffect.EffectType.greenhouse)
                .ifPresentOrElse(
                        e -> { /* 이미 당일 온실 있음 — 그대로 둠 */ },
                        () -> plotEffectRepository.save(TbPlotEffect.greenhouse(plot.getIdxPlot(), today))
                );
    }

    /** 비료: 그 밭 작물 물주기 즉시 단축 (effect row 안 남김, 즉시 소모) */
    private void applyFertilizer(TbPlot plot) {
        TbCrop crop = cropRepository.findByIdxPlot(plot.getIdxPlot())
                .orElseThrow(() -> CustomException.badRequest("이 밭에는 작물이 없습니다."));
        if (crop.getState() != TbCrop.State.growing) {
            throw CustomException.badRequest("성장 중인 작물에만 비료를 줄 수 있습니다.");
        }
        int remaining = crop.getTotal() - crop.getWatered();
        int boost = Math.min(Math.max((int) Math.ceil(remaining * 0.5), 1), MAX_FERT_BOOST);
        crop.boostWater(boost);
    }

    // 공통

    /** 대상 밭이 내 농장 밭인지 검증 */
    private TbPlot requireMyPlot(TbFarm farm, Long targetPlotId) {
        if (targetPlotId == null) {
            throw CustomException.badRequest("도구를 설치할 밭을 선택해 주세요.");
        }
        TbPlot plot = plotRepository.findById(targetPlotId)
                .orElseThrow(() -> CustomException.notFound("밭을 찾을 수 없습니다."));
        if (!plot.getIdxFarm().equals(farm.getIdxFarm())) {
            throw CustomException.forbidden("내 농장의 밭이 아닙니다.");
        }
        return plot;
    }

    private int currentLevel(Long idxUser) {
        return userRepository.findById(idxUser).map(TbUser::getLevel).orElse(1);
    }
}