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

    // 업적 집계용 누적 수확량 (판매해도 안 줄어드는 평생 카운터)
    @Column(name = "TotalHarvest", nullable = false)
    private int totalHarvest = 0;

    //@Column(name = "ScarecrowLeft", nullable = false)
    //private int scarecrowLeft = 0;

    // ✏️ TODO: addDrops/spendCoin 등
    
    /** 신규 유저 기본 농장 생성 */
    public static TbFarm createDefault(Long idxUser) {
        TbFarm f = new TbFarm();
        f.idxUser = idxUser;
        f.name = "내 농장";
        f.drops = 0;
        f.coin = 0;
        //f.scarecrowLeft = 0;
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
    
    /** 코인 차감(상점 구매). 부족하면 예외. */
    public void spendCoin(int amount) {
        if (amount <= 0) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("구매 수량이 올바르지 않습니다.");
        }
        if (coin < amount) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("코인이 부족합니다.");
        }
        coin -= amount;
    }

    /** 코인 적립(주민 판매 등) — 다음 작업에서 사용 예정 */
    public void earnCoin(int amount) {
        if (amount > 0) coin += amount;
    }
    
    /** 허수아비 방어 1회 소모. 남아있으면 true(방어 성공). */
    //public boolean useScarecrow() {
    //    if (scarecrowLeft > 0) {
    //        scarecrowLeft--;
    //        return true;
    //    }
    //    return false;
    //}
    
    /** 허수아비 비축(구매). */
    //public void addScarecrow(int count) {
    //    if (count > 0) scarecrowLeft += count;
    //}
    
    /** 물방울 적립 (할일 완료 / 레벨업 보너스). */
    public void addDrops(int amount) {
        if (amount > 0) this.drops += amount;
    }
    
    /** 누적 수확량 증가 (수확 시 호출). 업적 집계용. */
    public void addHarvestCount(int amount) {
        if (amount > 0) this.totalHarvest += amount;
    }
    
}