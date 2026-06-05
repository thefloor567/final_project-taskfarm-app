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
}