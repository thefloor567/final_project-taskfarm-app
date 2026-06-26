package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse.EffectDto;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse.EventDto;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse.PlotDto;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse.ThreatDto;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.team4.taskfarm.common.entity.farm.TbCoinLedger;
import com.team4.taskfarm.user.domain.farm.repository.TbCoinLedgerRepository;

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
    private final AuthUserRepository userRepository;
    private final TbPlotEffectRepository plotEffectRepository;
    private final TbEventTargetRepository eventTargetRepository;
    private final ThreatHandler threatHandler;
    private final FarmEventService farmEventService;
    private final WitherChecker witherChecker;
    private final TbCoinLedgerRepository coinLedgerRepository;

    /** 신규 유저 농장 기본 밭 개수 */
    private static final int DEFAULT_PLOT_COUNT = 6;

    /**
     * 농장 전체 스냅샷 조회.
     * 신규 유저는 농장/밭이 없으므로, 없으면 생성해서 반환한다(서버 권위).
     */
    @Transactional
    public FarmResponse getFarm(Long idxUser) {
        TbFarm farm = farmRepository.findByIdxUser(idxUser)
                .orElseGet(() -> createFarm(idxUser));

        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        // 1) 밭 목록 (슬롯 순)
        List<TbPlot> plots = plotRepository.findByIdxFarmOrderBySlotAsc(farm.getIdxFarm());
        List<Long> plotIds = plots.stream().map(TbPlot::getIdxPlot).toList();

        // 2) 밭에 심긴 작물 한 번에 → plotId 기준 맵
        Map<Long, TbCrop> cropByPlot = plotIds.isEmpty() ? new HashMap<>()
                : new HashMap<>(cropRepository.findByIdxPlotIn(plotIds).stream()
                    .collect(Collectors.toMap(TbCrop::getIdxPlot, c -> c, (a, b) -> a)));

        // 2-1) 시듦 판정 (lazy). 온실 effect 로 방어, 없으면 withered.
        witherChecker.applyWither(new ArrayList<>(cropByPlot.values()));

        // 3) 오늘 이벤트 (위협 처리에 필요하므로 먼저)
        TbFarmEvent todayEvent = farmEventService.getTodayEvent(idxUser);

        // 3-1) 위협 이벤트 처리 (까마귀/가뭄). 즉시 타격, 이미 처리됐으면 스킵.
        //      ⚠️ 작물을 제거/시듦 시킬 수 있으므로 PlotDto 매핑 '전'에 실행.
        threatHandler.handleTodayThreat(todayEvent, plots, cropByPlot);

        // 3-2) 위협 처리로 작물이 삭제/변경됐을 수 있으므로 다시 조회 (삭제분 자동 반영)
        //      람다에서 쓰므로 새 final 변수에 담는다.
        final Map<Long, TbCrop> cropAfterThreat = plotIds.isEmpty() ? Map.of()
                : cropRepository.findByIdxPlotIn(plotIds).stream()
                    .collect(Collectors.toMap(TbCrop::getIdxPlot, c -> c, (a, b) -> a));

        // 4) 작물 이름·코드용 씨앗 한 번에 (N+1 방지)
        List<Long> seedIds = cropAfterThreat.values().stream().map(TbCrop::getIdxSeed).distinct().toList();
        Map<Long, TbSeed> seedById = seedIds.isEmpty() ? Map.of()
                : seedRepository.findByIdxSeedIn(seedIds).stream()
                    .collect(Collectors.toMap(TbSeed::getIdxSeed, s -> s));

        // 5) 밭별 도구 효과 (tbPlotEffect) — plotId 기준 그룹핑 (만료 제외)
        LocalDate today = LocalDate.now();
        Map<Long, List<EffectDto>> effectsByPlot = plotIds.isEmpty() ? Map.of()
                : plotEffectRepository.findByIdxPlotIn(plotIds).stream()
                    .filter(e -> !e.isExpired(today))
                    .collect(Collectors.groupingBy(
                            TbPlotEffect::getIdxPlot,
                            Collectors.mapping(EffectDto::of, Collectors.toList())));

        // 6) 밭별 위협 상태 (tbEventTarget) — plotId 기준 맵
        Map<Long, TbEventTarget> targetByPlot = eventTargetRepository
                .findByIdxFarmEvent(todayEvent.getIdxFarmEvent()).stream()
                .collect(Collectors.toMap(TbEventTarget::getIdxPlot, t -> t, (a, b) -> a));

        // 7) PlotDto 매핑 (위협 처리 후의 최신 상태)
        List<PlotDto> plotDtos = plots.stream()
                .map(p -> {
                    TbCrop crop = cropAfterThreat.get(p.getIdxPlot());
                    TbSeed seed = (crop == null) ? null : seedById.get(crop.getIdxSeed());
                    String name = (seed == null) ? null : seed.getName();
                    String code = (seed == null) ? null : seed.getCode();

                    List<EffectDto> effects = effectsByPlot.getOrDefault(p.getIdxPlot(), List.of());

                    // 위협 표시: 그 밭에 작물이 있을 때만 (작물 없으면 위협 뱃지 안 그림).
                    // 폭풍/까마귀가 작물을 제거한 뒤에도 tbEventTarget 은 남으므로,
                    // 빈 밭에 유령 위협 뱃지가 뜨는 것을 방지.
                    TbEventTarget tgt = targetByPlot.get(p.getIdxPlot());
                    ThreatDto threat = (tgt == null || crop == null) ? null
                            : ThreatDto.of(todayEvent.getEventKey(), tgt.isDefended());

                    return PlotDto.of(p, crop, name, code, effects, threat);
                })
                .toList();

        // 8) 수확 보유 작물 총합
        int cropCount = cropInvRepository.findByIdxFarm(farm.getIdxFarm()).stream()
                .mapToInt(TbCropInv::getQty).sum();

        EventDto eventDto = toEventDto(todayEvent);

        return FarmResponse.builder()
                .farmId(farm.getIdxFarm())
                .name(farm.getName())
                .drops(farm.getDrops())
                .coin(farm.getCoin())
                .cropCount(cropCount)
                .level(user.getLevel())
                .exp(user.getExp())
                .streak(user.getStreak())
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

        // 신규 가입 보너스 100코인 — 잔고 적립 + 원장 기록(이력 남김)
        final int SIGNUP_BONUS = 100;
        farm.earnCoin(SIGNUP_BONUS);
        coinLedgerRepository.save(
            TbCoinLedger.earn(farm.getIdxFarm(), SIGNUP_BONUS, "SIGNUP_BONUS", null)
        );

        return farm;
    }

    /** 이벤트 키 → 표시 문구 매핑(임시 카탈로그). */
    private static class EventCatalog {
        static String titleOf(String key) {
            return switch (key) {
                case "harvest_bonus" -> "🌟 풍년의 날";
                case "rain"          -> "🌧️ 단비";
                case "drought"       -> "☀️ 가뭄";
                case "storm"         -> "🌪️ 폭풍";
                case "crow"          -> "🐦‍⬛ 까마귀 떼";
                case "pest"          -> "🐛 해충";
                case "fertile"       -> "🌱 비옥한 땅";
                default              -> "🌤️ 평범한 하루";
            };
        }
        static String descOf(String key) {
            return switch (key) {
                case "harvest_bonus" -> "오늘 수확하면 작물 +1 보너스!";
                case "rain"          -> "물방울 소비 없이 물주기 가능!";
                case "drought"       -> "온실 없는 밭의 작물이 시들어요.";
                case "storm"         -> "작물이 위험해요.";
                case "crow"          -> "허수아비 없는 밭의 작물을 까마귀가 노려요!";
                case "pest"          -> "해충이 작물을 노려요.";
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