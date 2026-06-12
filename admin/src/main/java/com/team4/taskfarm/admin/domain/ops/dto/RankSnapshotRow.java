package com.team4.taskfarm.admin.domain.ops.dto;

/**
 * 랭킹 스냅샷 조회용 projection (tbRankSnapshot + tbUser 닉네임).
 */
public interface RankSnapshotRow {
    int getRanking();
    Long getUserId();
    String getNickname();
    int getWeeklyExp();
}
