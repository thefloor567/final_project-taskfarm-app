package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.farm.dto.ToolShopItemResponse;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 도구 상점 (서버 권위). 도구 3종: 허수아비/비료/온실.
 * 도구 인벤 테이블 없음 — 구매 즉시 효과 적용(즉시형). 허수아비만 farm.ScarecrowLeft 비축.
 * 기획서(team4-tools-handoff) §3·§4 반영.
 */
@Service
@RequiredArgsConstructor
public class ToolShopService {

    private final TbFarmRepository farmRepository;
    private final TbUserRepository userRepository;
    private final TbToolRepository toolRepository;
    private final TbCropRepository cropRepository;
    private final TbPlotRepository plotRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;
    private final TbFarmEventRepository farmEventRepository;

    // ── 기획서 §3 코드 상수 ──
    private static final Map<String, Integer> TOOL_UNLOCK = Map.of(
            "scarecrow", 2, "fertilizer", 3, "greenhouse", 5
    );
    private static final int MAX_FERT_BOOST = 2;   // 비료 1회 최대 단축 물주기(캡)

    /** 온실로 막는 유해 이벤트 키 (시듦/가뭄 등). tbEventConfig EventKey 기준. */
    private static final Set<String> WEATHER_HARMFUL = Set.of("drought", "wither", "storm");

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
     * 도구 구매 + 즉시 효과. Type별 분기.
     * @param targetPlotId 비료 전용(대상 밭). 그 외 null.
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
        tool.purchase();              // 판매중·재고 검증 + 재고 차감
        farm.spendCoin(tool.getPrice()); // 코인 차감(부족 시 예외)

        // Type별 효과
        switch (tool.getType()) {
            case scarecrow  -> applyScarecrow(farm, tool);
            case fertilizer -> applyFertilizer(farm, tool, targetPlotId);
            case greenhouse -> applyGreenhouse(farm, idxUser, tool);
        }

        // 코인 원장 SPEND 기록
        coinLedgerRepository.save(
                TbCoinLedger.spend(farm.getIdxFarm(), tool.getPrice(),
                        "BUY_" + tool.getType().name().toUpperCase(), tool.getIdxTool()));
    }

    // ── Type별 효과 (기획서 §4) ──

    /** 허수아비: 방어 횟수 비축 (Uses 만큼) */
    private void applyScarecrow(TbFarm farm, TbTool tool) {
        farm.addScarecrow(tool.getUses());   // 예: +3
    }

    /** 비료: 대상 밭 작물의 남은 물주기 50% 즉시 채움(캡 2, 최소 1) */
    private void applyFertilizer(TbFarm farm, TbTool tool, Long targetPlotId) {
        if (targetPlotId == null) {
            throw CustomException.badRequest("비료를 줄 작물을 선택해 주세요.");
        }
        // 서버 권위: 그 밭이 내 농장 밭인지 검증 (남의 밭 plotId 조작 차단)
        TbPlot plot = plotRepository.findById(targetPlotId)
                .orElseThrow(() -> CustomException.notFound("밭을 찾을 수 없습니다."));
        if (!plot.getIdxFarm().equals(farm.getIdxFarm())) {
            throw CustomException.forbidden("내 농장의 밭이 아닙니다.");
        }

        TbCrop crop = cropRepository.findByIdxPlot(plot.getIdxPlot())
                .orElseThrow(() -> CustomException.badRequest("이 밭에는 작물이 없습니다."));
        if (crop.getState() != TbCrop.State.growing) {
            throw CustomException.badRequest("성장 중인 작물에만 비료를 줄 수 있습니다.");
        }

        int remaining = crop.getTotal() - crop.getWatered();
        int boost = Math.min(Math.max((int) Math.ceil(remaining * 0.5), 1), MAX_FERT_BOOST);
        crop.boostWater(boost);   // watered 증가 + 다 차면 ready, plantDate 갱신
    }

    /** 온실: 당일 농장 보호 — 오늘 유해 이벤트를 dismiss */
    private void applyGreenhouse(TbFarm farm, Long idxUser, TbTool tool) {
        farmEventRepository.findByIdxUserAndEventDate(idxUser, LocalDate.now())
                .ifPresent(ev -> {
                    if (WEATHER_HARMFUL.contains(ev.getEventKey())) {
                        ev.dismiss();
                    }
                });
        // 당일 시듦 판정은 dismiss된 이벤트를 보고 스킵(시듦 로직과 연동).
    }

    // ── 공통 ──

    private int currentLevel(Long idxUser) {
        return userRepository.findById(idxUser).map(TbUser::getLevel).orElse(1);
    }
}