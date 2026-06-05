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
    
    /** 신규 유저 기본 농장 생성 */
    public static TbFarm createDefault(Long idxUser) {
        TbFarm f = new TbFarm();
        f.idxUser = idxUser;
        f.name = "내 농장";
        f.drops = 0;
        f.coin = 0;
        f.scarecrowLeft = 0;
        return f;
    }
    
    /** 물방울 차감(물주기). 부족하면 예외. */
    public void spendDrops(int amount) {
        if (drops < amount) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("물방울이 부족합니다.");
        }
        drops -= amount;
    }
}