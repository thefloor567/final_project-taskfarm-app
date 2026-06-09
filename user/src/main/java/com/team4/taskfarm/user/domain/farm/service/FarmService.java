package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse.EventDto;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse.PlotDto;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final TbFarmRepository farmRepository;
    private final TbPlotRepository plotRepository;
    private final TbCropRepository cropRepository;
    private final TbSeedRepository seedRepository;
    private final TbCropInvRepository cropInvRepository;
    private final TbFarmEventRepository farmEventRepository;

    /** 신규 유저 농장 기본 밭 개수 */
    private static final int DEFAULT_PLOT_COUNT = 6;
    
    /** 이벤트 체크 */
    private final FarmEventService farmEventService;
    
    /** 시듦 판정 */
    private final WitherChecker witherChecker;

    /**
     * 농장 전체 스냅샷 조회.
     * 신규 유저는 농장/밭이 없으므로, 없으면 생성해서 반환한다(서버 권위).
     */
    @Transactional
    public FarmResponse getFarm(Long idxUser) {
        TbFarm farm = farmRepository.findByIdxUser(idxUser)
                .orElseGet(() -> createFarm(idxUser));

        // 1) 밭 목록 (슬롯 순)
        List<TbPlot> plots = plotRepository.findByIdxFarmOrderBySlotAsc(farm.getIdxFarm());
        List<Long> plotIds = plots.stream().map(TbPlot::getIdxPlot).toList();

        // 2) 밭에 심긴 작물 한 번에 → plotId 기준 맵
        Map<Long, TbCrop> cropByPlot = plotIds.isEmpty() ? Map.of()
                : cropRepository.findByIdxPlotIn(plotIds).stream()
                    .collect(Collectors.toMap(TbCrop::getIdxPlot, c -> c, (a, b) -> a));
        
        // 2-1) 시듦 판정 (lazy). growing 중 기준시간 초과분 처리. 허수아비 있으면 방어.
        witherChecker.applyWither(farm, new ArrayList<>(cropByPlot.values()));
        // @Transactional 이라 변경은 자동 저장됨. 이후 PlotDto 매핑은 갱신된 state 를 그대로 사용.

        // 3) 작물 이름 표시용 씨앗 한 번에 → seedId 기준 맵 (N+1 방지)
        List<Long> seedIds = cropByPlot.values().stream().map(TbCrop::getIdxSeed).distinct().toList();
        Map<Long, String> seedNameById = seedIds.isEmpty() ? Map.of()
                : seedRepository.findByIdxSeedIn(seedIds).stream()
                    .collect(Collectors.toMap(TbSeed::getIdxSeed, TbSeed::getName));

        List<PlotDto> plotDtos = plots.stream()
                .map(p -> {
                    TbCrop crop = cropByPlot.get(p.getIdxPlot());
                    String name = crop == null ? null : seedNameById.get(crop.getIdxSeed());
                    return PlotDto.of(p, crop, name);
                })
                .toList();

        // 4) 수확 보유 작물 총합
        int cropCount = cropInvRepository.findByIdxFarm(farm.getIdxFarm()).stream()
                .mapToInt(TbCropInv::getQty).sum();

        // 5) 오늘의 이벤트 (없으면 시드로 생성 — 스케줄러 없는 lazy)
        TbFarmEvent todayEvent = farmEventService.getTodayEvent(idxUser);
        EventDto eventDto = toEventDto(todayEvent);

        return FarmResponse.builder()
                .farmId(farm.getIdxFarm())
                .name(farm.getName())
                .drops(farm.getDrops())
                .coin(farm.getCoin())
                .scarecrowLeft(farm.getScarecrowLeft())
                .cropCount(cropCount)
                .event(eventDto)
                .plots(plotDtos)
                .build();
    }

    /** 신규 유저용 농장 + 기본 밭 생성 */
    private TbFarm createFarm(Long idxUser) {
        TbFarm farm = farmRepository.save(TbFarm.createDefault(idxUser));
        for (int slot = 1; slot <= DEFAULT_PLOT_COUNT; slot++) {
            plotRepository.save(TbPlot.create(farm.getIdxFarm(), slot));
        }
        return farm;
    }

    /**
     * 이벤트 키 → 표시 문구 매핑(임시 카탈로그).
     * TODO: 이벤트 도메인 본격 작업 시 tbEventConfig 기반으로 이관.
     */
    private static class EventCatalog {
        static String titleOf(String key) {
            return switch (key) {
                case "harvest_bonus" -> "🌟 풍년의 날";
                case "rain"          -> "🌧️ 단비";
                case "drought"       -> "☀️ 가뭄";
                case "storm"         -> "🌪️ 폭풍";
                case "fertile"       -> "🌱 비옥한 땅";
                default              -> "🌤️ 평범한 하루";   // normal 포함
            };
        }
        static String descOf(String key) {
            return switch (key) {
                case "harvest_bonus" -> "오늘 수확하면 작물 +1 보너스!";
                case "rain"          -> "물방울 소비 없이 물주기 가능!";
                case "drought"       -> "작물이 더 빨리 시들어요.";
                case "storm"         -> "허수아비가 없으면 작물이 위험해요.";
                case "fertile"       -> "오늘 심는 작물은 물주기 1회 감소!";
                default              -> "오늘은 특별한 일이 없네요.";
            };
        }
    }

    private EventDto toEventDto(TbFarmEvent e) {
        return EventDto.builder()
                .eventKey(e.getEventKey())
                .title(EventCatalog.titleOf(e.getEventKey()))
                .desc(EventCatalog.descOf(e.getEventKey()))
                .dismissed(e.isDismissed())
                .build();
    }
}