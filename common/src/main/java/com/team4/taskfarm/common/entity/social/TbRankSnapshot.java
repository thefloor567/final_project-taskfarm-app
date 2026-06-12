package com.team4.taskfarm.common.entity.social;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주간 확정 랭킹 스냅샷 (tbRankSnapshot).
 * - 실시간 순위는 Redis Sorted Set, DB는 매주 마감된 "확정 순위"만 영구 기록.
 * - 주간 리셋 스케줄러(분산락)가 1주에 한 번 insert.
 * - 치팅 감사: 비정상 주간 Exp 추적.
 *
 * ⚠️ 'Rank'는 MySQL8 예약어 → 컬럼명 'Ranking' 사용 (필드는 ranking).
 * ⚠️ 길드 기능 제외 → Scope/ScopeRefId 컬럼 제거 (전체 랭킹만).
 */
@Entity
@Table(name = "tbRankSnapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbRankSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_RankSnapshot")
    private Long idxRankSnapshot;

    @Column(name = "Period", nullable = false, length = 10)
    private String period;   // 예: "2026W24"

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

    @Column(name = "Ranking", nullable = false)
    private int ranking;

    @Column(name = "WeeklyExp", nullable = false)
    private int weeklyExp = 0;

    public static TbRankSnapshot of(String period, Long idxUser, int ranking, int weeklyExp) {
        TbRankSnapshot s = new TbRankSnapshot();
        s.period = period;
        s.idxUser = idxUser;
        s.ranking = ranking;
        s.weeklyExp = weeklyExp;
        return s;
    }
}
