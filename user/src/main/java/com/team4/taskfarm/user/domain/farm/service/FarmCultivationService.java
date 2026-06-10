package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.farm.dto.CropInvResponse;
import com.team4.taskfarm.user.domain.farm.dto.SeedInvResponse;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 밭 재배 로직 (서버 권위).
 * 모든 동작은 "① 내 농장 확인 → ② 밭 소유 검증 → ③ 재화/상태 검증 → ④ 차감·변경"을 한 트랜잭션으로 처리.
 */
@Service
@RequiredArgsConstructor
public class FarmCultivationService {

    private final TbFarmRepository farmRepository;
    private final TbPlotRepository plotRepository;
    private final TbCropRepository cropRepository;
    private final TbSeedRepository seedRepository;
    private final TbSeedInvRepository seedInvRepository;
    private final TbCropInvRepository cropInvRepository;

    /** 물주기 1회당 물방울 비용 */
    private static final int WATER_COST = 1;

    /**
     * 씨앗 심기. 보유 씨앗 1개 소비 → 밭에 growing 작물 생성.
     */
    @Transactional
    public void plantSeed(Long idxUser, Long plotId, Long seedId) {
        TbFarm farm = getMyFarm(idxUser);
        TbPlot plot = getMyPlot(farm, plotId);

        // 이미 작물이 있으면 못 심음
        cropRepository.findByIdxPlot(plot.getIdxPlot()).ifPresent(c -> {
            throw CustomException.badRequest("이미 작물이 자라고 있는 밭입니다.");
        });

        // 씨앗 마스터(물주기 횟수 확인)
        TbSeed seed = seedRepository.findById(seedId)
                .orElseThrow(() -> CustomException.notFound("씨앗 정보를 찾을 수 없습니다."));

        // 보유 씨앗 차감 (없으면 엔티티에서 예외)
        TbSeedInv inv = seedInvRepository.findByIdxFarmAndIdxSeed(farm.getIdxFarm(), seedId)
                .orElseThrow(() -> CustomException.badRequest("보유한 씨앗이 없습니다."));
        inv.consumeOne();

        cropRepository.save(TbCrop.plant(plot.getIdxPlot(), seedId, seed.getWaters()));
    }

    /**
     * 물주기. 물방울 1 소비 → watered+1. 다 채우면 ready.
     */
    @Transactional
    public void waterPlot(Long idxUser, Long plotId) {
        TbFarm farm = getMyFarm(idxUser);
        TbPlot plot = getMyPlot(farm, plotId);

        TbCrop crop = cropRepository.findByIdxPlot(plot.getIdxPlot())
                .orElseThrow(() -> CustomException.badRequest("이 밭에는 작물이 없습니다."));

        farm.spendDrops(WATER_COST);  // 물방울 부족 시 예외
        crop.water();                 // growing 아니면 예외, total 채우면 ready
    }

    /**
     * 수확. ready 작물 → 작물 인벤토리 적립 + 밭 비우기.
     * 코인은 주민 주문 이행 때 지급(여기선 X). 풍년 보너스는 이벤트 도메인 연동 시.
     */
    @Transactional
    public void harvestPlot(Long idxUser, Long plotId) {
        TbFarm farm = getMyFarm(idxUser);
        TbPlot plot = getMyPlot(farm, plotId);

        TbCrop crop = cropRepository.findByIdxPlot(plot.getIdxPlot())
                .orElseThrow(() -> CustomException.badRequest("이 밭에는 작물이 없습니다."));

        crop.harvest();  // ready 아니면 예외

        int amount = 1;  // TODO: 풍년 이벤트면 +1 (이벤트 도메인 연동)

        // 작물 인벤토리 적립 (있으면 +, 없으면 신규)
        cropInvRepository.findByIdxFarmAndIdxSeed(farm.getIdxFarm(), crop.getIdxSeed())
                .ifPresentOrElse(
                        ci -> ci.add(amount),
                        () -> cropInvRepository.save(
                                TbCropInv.create(farm.getIdxFarm(), crop.getIdxSeed(), amount))
                );

        cropRepository.delete(crop);  // 밭 비우기
    }

    /**
     * 시든 작물 제거. withered 상태만 비울 수 있음.
     */
    @Transactional
    public void removeCrop(Long idxUser, Long plotId) {
        TbFarm farm = getMyFarm(idxUser);
        TbPlot plot = getMyPlot(farm, plotId);

        TbCrop crop = cropRepository.findByIdxPlot(plot.getIdxPlot())
                .orElseThrow(() -> CustomException.badRequest("이 밭에는 작물이 없습니다."));

        if (crop.getState() != TbCrop.State.withered) {
            throw CustomException.badRequest("시든 작물만 제거할 수 있습니다.");
        }
        cropRepository.delete(crop);
    }

    /**
     * 보유 씨앗 목록 (심을 씨앗 고르기 UI용).
     */
    @Transactional(readOnly = true)
    public List<SeedInvResponse> getSeedInventoryList(Long idxUser) {
        TbFarm farm = getMyFarm(idxUser);

        List<TbSeedInv> invs = seedInvRepository.findByIdxFarm(farm.getIdxFarm()).stream()
                .filter(i -> i.getQty() > 0)
                .toList();
        if (invs.isEmpty()) return List.of();

        // 씨앗 이름·물주기수 한 번에 (N+1 방지)
        List<Long> seedIds = invs.stream().map(TbSeedInv::getIdxSeed).distinct().toList();
        Map<Long, TbSeed> seedById = seedRepository.findByIdxSeedIn(seedIds).stream()
                .collect(Collectors.toMap(TbSeed::getIdxSeed, s -> s));

        return invs.stream().map(i -> {
            TbSeed s = seedById.get(i.getIdxSeed());
            return SeedInvResponse.builder()
                    .seedId(i.getIdxSeed())
                    .name(s != null ? s.getName() : "씨앗")
                    .waters(s != null ? s.getWaters() : 0)
                    .qty(i.getQty())
                    .build();
        }).toList();
    }
    
    /**
     * 보유 수확 작물 목록 (인벤토리 화면).
     */
    @Transactional(readOnly = true)
    public List<CropInvResponse> getCropInventoryList(Long idxUser) {
        TbFarm farm = farmRepository.findByIdxUser(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));

        List<TbCropInv> invs = cropInvRepository.findByIdxFarm(farm.getIdxFarm()).stream()
                .filter(i -> i.getQty() > 0)
                .toList();
        if (invs.isEmpty()) return List.of();

        // 작물 이름·코드 한 번에 (N+1 방지)
        List<Long> seedIds = invs.stream().map(TbCropInv::getIdxSeed).distinct().toList();
        Map<Long, TbSeed> seedById = seedRepository.findByIdxSeedIn(seedIds).stream()
                .collect(Collectors.toMap(TbSeed::getIdxSeed, s -> s));

        return invs.stream().map(i -> {
            TbSeed s = seedById.get(i.getIdxSeed());
            return CropInvResponse.builder()
                    .seedId(i.getIdxSeed())
                    .name(s != null ? s.getName() : "작물")
                    .code(s != null ? s.getCode() : null)
                    .count(i.getQty())
                    .build();
        }).toList();
    }

    // ── 공통: 서버 권위 검증 ──

    /** 내 농장 조회 (없으면 예외 — getFarm을 먼저 호출했다는 전제) */
    private TbFarm getMyFarm(Long idxUser) {
        return farmRepository.findByIdxUser(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));
    }

    /** 이 밭이 정말 내 농장 밭인지 검증 (핵심 방어: 남의 밭 plotId 조작 차단) */
    private TbPlot getMyPlot(TbFarm farm, Long plotId) {
        TbPlot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> CustomException.notFound("밭을 찾을 수 없습니다."));
        if (!plot.getIdxFarm().equals(farm.getIdxFarm())) {
            throw CustomException.forbidden("내 농장의 밭이 아닙니다.");
        }
        return plot;
    }
}