package com.team4.taskfarm.user.domain.farm.dto;

import com.team4.taskfarm.common.entity.farm.TbCrop;
import com.team4.taskfarm.common.entity.farm.TbPlot;
import com.team4.taskfarm.common.entity.farm.TbPlotEffect;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 농장 전체 스냅샷 (GET /api/farm).
 * 밭 상태 + 물방울(drops) + 코인 + 진행 이벤트를 한 번에 내려준다.
 * 프론트(farm.html)는 이 한 번의 응답으로 화면 전체를 렌더한다.
 */
@Getter
@Builder
public class FarmResponse {

    private Long farmId;
    private String name;
    private int drops;        // 물방울 (물주기 재화)
    private int coin;         // 코인 (상점 재화)
    private int cropCount;    // 수확 보유 작물 총합
    private int level;        // 유저 레벨 (상점 해금 표시용)
    private int exp;          // 유저 경험치 (XP바 표시용)
    private int streak;       // 연속 완료일 (이벤트 가중치)

    private EventDto event;   // 오늘의 이벤트 (없으면 null)
    private List<PlotDto> plots;

    @Getter
    @Builder
    public static class PlotDto {
        private Long plotId;
        private int slot;
        private String status;     // empty / growing / ready / withered
        private String cropName;   // 심긴 작물 이름 (없으면 null)
        private String code;       // 작물 종류 코드 (TbSeed.code) — 이모지 매핑용
        private Long seedId;
        private int watered;
        private int total;
        private List<EffectDto> effects;   // 밭별 설치 도구 (tbPlotEffect)
        private ThreatDto threat;          // 오늘 이 밭이 노려진 위협 (tbEventTarget)

        /**
         * 밭 + (선택)작물 + 작물코드 + 효과 + 위협 → DTO. crop이 null이면 빈 밭.
         */
        public static PlotDto of(TbPlot plot, TbCrop crop, String cropName, String code,
                                 List<EffectDto> effects, ThreatDto threat) {
            PlotDtoBuilder b = PlotDto.builder()
                    .plotId(plot.getIdxPlot())
                    .slot(plot.getSlot())
                    .effects(effects)
                    .threat(threat);
            if (crop == null) {
                return b.status("empty").watered(0).total(0).build();
            }
            return b.status(crop.getState().name())
                    .cropName(cropName)
                    .code(code)
                    .seedId(crop.getIdxSeed())
                    .watered(crop.getWatered())
                    .total(crop.getTotal())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class EffectDto {
        private String type;        // scarecrow / greenhouse / fertilizer
        private Integer remainUses; // 허수아비 남은 횟수 (온실은 null)

        public static EffectDto of(TbPlotEffect e) {
            return EffectDto.builder()
                    .type(e.getEffectType().name())
                    .remainUses(e.getEffectType() == TbPlotEffect.EffectType.scarecrow
                            ? e.getRemainUses() : null)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class EventDto {
        private String eventKey;
        private String title;
        private String desc;
        private boolean dismissed;
    }

    @Getter
    @Builder
    public static class ThreatDto {
        private String type;       // crow / drought / storm / pest (eventKey)
        private boolean defended;  // 허수아비·온실로 막았는지 (IsDefended)

        public static ThreatDto of(String eventKey, boolean defended) {
            return ThreatDto.builder().type(eventKey).defended(defended).build();
        }
    }
}