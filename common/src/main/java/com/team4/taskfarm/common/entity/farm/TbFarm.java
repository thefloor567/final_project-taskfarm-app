package com.team4.taskfarm.common.entity.farm;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 농장 (tbFarm). 유저당 1:1. 농장 화폐(Drops/Coin)는 여기, 성장치(Exp/Level)는 tbUser.
 */
@Entity
@Table(name = "tbFarm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbFarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Farm")
    private Long idxFarm;

    @Column(name = "Idx_User", nullable = false, unique = true)
    private Long idxUser;

    @Column(name = "Name", nullable = false, length = 50)
    private String name = "내 농장";

    @Column(name = "Drops", nullable = false)
    private int drops = 0;

    @Column(name = "Coin", nullable = false)
    private int coin = 0;

    @Column(name = "ScarecrowLeft", nullable = false)
    private int scarecrowLeft = 0;

    // ✏️ TODO: addDrops/spendCoin 등
}