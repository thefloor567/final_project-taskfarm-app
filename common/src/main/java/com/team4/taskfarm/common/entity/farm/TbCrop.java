package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 작물 (tbCrop). 밭에 심긴 것. Watered==Total이면 ready.
 */
@Entity
@Table(name = "tbCrop")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbCrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Crop")
    private Long idxCrop;

    @Column(name = "Idx_Plot", nullable = false)
    private Long idxPlot;

    @Column(name = "Idx_Seed", nullable = false)
    private Long idxSeed;

    @Enumerated(EnumType.STRING)
    @Column(name = "State", nullable = false)
    private State state = State.growing;

    @Column(name = "Watered", nullable = false)
    private int watered = 0;

    @Column(name = "Total", nullable = false)
    private int total;

    @Column(name = "PlantDate", nullable = false)
    private LocalDateTime plantDate;

    @Column(name = "HarvestDate")
    private LocalDateTime harvestDate;

    public enum State { growing, ready, withered }

    // ✏️ TODO: water(), harvest()
    
    /** 씨앗 심기: 밭에 새 작물 생성 (growing 시작) */
    public static TbCrop plant(Long idxPlot, Long idxSeed, int waters) {
        TbCrop c = new TbCrop();
        c.idxPlot = idxPlot;
        c.idxSeed = idxSeed;
        c.state = State.growing;
        c.watered = 0;
        c.total = waters;        // 씨앗이 요구하는 물주기 횟수
        c.plantDate = java.time.LocalDateTime.now();
        return c;
    }

    /** 물주기 1회. plantDate 갱신(시듦 타이머 리셋). 다 채우면 ready. */
    public void water() {
        if (state != State.growing) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("물을 줄 수 있는 상태가 아닙니다.");
        }
        watered++;
        plantDate = java.time.LocalDateTime.now();   // ← 시듦 타이머 리셋
        if (watered >= total) {
            state = State.ready;
        }
    }
    
    /** 비료 등으로 물주기를 여러 칸 즉시 채움. plantDate도 갱신. */
    public void boostWater(int amount) {
        if (state != State.growing) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("성장 중인 작물만 가능합니다.");
        }
        watered = Math.min(watered + amount, total);
        plantDate = java.time.LocalDateTime.now();
        if (watered >= total) {
            state = State.ready;
        }
    }
    
    /**
     * 시듦 판정 (lazy). growing 상태에서 기준 시간(분) 초과 시 시들 차례.
     * @return true = "시들 때가 됨"(호출측에서 허수아비 방어/시듦 처리 결정)
     */
    public boolean isWitherDue(long witherMinutes) {
        if (state != State.growing) return false;
        return plantDate.plusMinutes(witherMinutes).isBefore(java.time.LocalDateTime.now());
    }
    
    /** 실제로 시들게 만듦 */
    public void wither() {
        this.state = State.withered;
    }
    
    /** 허수아비가 방어 → 시듦 타이머 리셋(시들지 않음) */
    public void protectFromWither() {
        this.plantDate = java.time.LocalDateTime.now();
    }

    /** 수확: ready 상태에서만. 수확일 기록. */
    public void harvest() {
        if (state != State.ready) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("아직 수확할 수 없습니다.");
        }
        harvestDate = java.time.LocalDateTime.now();
    }
}