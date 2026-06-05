package com.team4.taskfarm.user.domain.farm.dto;

import com.team4.taskfarm.common.entity.farm.TbCrop;
import com.team4.taskfarm.common.entity.farm.TbPlot;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 농장 전체 스냅샷 (GET /api/farm).
 * 밭 상태 + 물방울(drops) + 코인 + 허수아비 + 진행 이벤트를 한 번에 내려준다.
 * 프론트(farm.html)는 이 한 번의 응답으로 화면 전체를 렌더한다.
 */
@Getter
@Builder
public class FarmResponse {

    private Long farmId;
    private String name;
    private int drops;        // 물방울 (물주기 재화)
    private int coin;         // 코인 (상점 재화)
    private int scarecrowLeft;// 남은 허수아비 방어 횟수
    private int cropCount;    // 수확 보유 작물 총합

    private EventDto event;   // 오늘의 이벤트 (없으면 null)
    private List<PlotDto> plots;

    @Getter
    @Builder
    public static class PlotDto {
        private Long plotId;
        private int slot;
        private String status;   // empty / growing / ready / withered
        private String cropName; // 심긴 작물 이름 (없으면 null)
        private Long seedId;
        private int watered;
        private int total;

        /** 밭 + (선택)작물 → DTO. crop이 null이면 빈 밭. */
        public static PlotDto of(TbPlot plot, TbCrop crop, String cropName) {
            PlotDtoBuilder b = PlotDto.builder()
                    .plotId(plot.getIdxPlot())
                    .slot(plot.getSlot());
            if (crop == null) {
                return b.status("empty").watered(0).total(0).build();
            }
            return b.status(crop.getState().name())
                    .cropName(cropName)
                    .seedId(crop.getIdxSeed())
                    .watered(crop.getWatered())
                    .total(crop.getTotal())
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
}