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

    /** 물주기 1회. 다 채우면 ready로 전환. 이미 다 줬으면 예외. */
    public void water() {
        if (state != State.growing) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("물을 줄 수 있는 상태가 아닙니다.");
        }
        watered++;
        if (watered >= total) {
            state = State.ready;
        }
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