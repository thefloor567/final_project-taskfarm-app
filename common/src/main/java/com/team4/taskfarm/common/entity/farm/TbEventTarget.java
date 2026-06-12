package com.team4.taskfarm.common.entity.farm;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이벤트가 노린 밭 (tbEventTarget).
 * - tbFarmEvent(오늘의 이벤트) ↔ tbPlot(노린 밭) 다대다 정규화.
 * - scope=none이면 row 없음 / one=1행 / multi=2~3행 / all=작물밭 전부.
 * - IsDefended: 그 밭이 허수아비·온실로 방어됐는지 → 방어 통계·업적 집계원.
 *   (업적 crow_defended / drought_survived 가 이 값을 카운트)
 * - (Idx_FarmEvent, Idx_Plot) UNIQUE 로 같은 이벤트가 같은 밭 중복 타격 방지.
 *
 * ※ BaseEntity의 UpdateDate는 쓰지 않지만(이벤트 타겟은 당일 고정),
 *   일관성을 위해 상속 유지. createDate만 의미 있음.
 */
@Entity
@Table(name = "tbEventTarget")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbEventTarget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_EventTarget")
    private Long idxEventTarget;

    @Column(name = "Idx_FarmEvent", nullable = false)
    private Long idxFarmEvent;

    @Column(name = "Idx_Plot", nullable = false)
    private Long idxPlot;

    @Column(name = "IsDefended", nullable = false)
    private boolean isDefended = false;

    /** 이벤트가 특정 밭을 노린 타겟 생성. */
    public static TbEventTarget of(Long idxFarmEvent, Long idxPlot) {
        TbEventTarget t = new TbEventTarget();
        t.idxFarmEvent = idxFarmEvent;
        t.idxPlot = idxPlot;
        t.isDefended = false;
        return t;
    }

    /** 방어 성공 표시 (허수아비/온실이 막았을 때). */
    public void markDefended() {
        this.isDefended = true;
    }
}