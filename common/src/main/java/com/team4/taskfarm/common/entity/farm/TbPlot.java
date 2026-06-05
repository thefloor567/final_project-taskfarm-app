package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 밭 슬롯 (tbPlot). 고정 칸. 농장 내 슬롯 유일(UK).
 */
@Entity
@Table(name = "tbPlot")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbPlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Plot")
    private Long idxPlot;

    @Column(name = "Idx_Farm", nullable = false)
    private Long idxFarm;

    @Column(name = "Slot", nullable = false)
    private int slot;

    @CreatedDate
    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createDate;
    
    
    /** 농장 밭 슬롯 생성 */
    public static TbPlot create(Long idxFarm, int slot) {
        TbPlot p = new TbPlot();
        p.idxFarm = idxFarm;
        p.slot = slot;
        return p;
    }
}