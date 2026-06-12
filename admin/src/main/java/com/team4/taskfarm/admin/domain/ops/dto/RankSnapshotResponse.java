package com.team4.taskfarm.admin.domain.ops.dto;

import lombok.Getter;

@Getter
public class RankSnapshotResponse {
    private final int rank;
    private final Long userId;
    private final String nickname;
    private final int weeklyExp;

    public RankSnapshotResponse(RankSnapshotRow r) {
        this.rank = r.getRanking();
        this.userId = r.getUserId();
        this.nickname = r.getNickname();
        this.weeklyExp = r.getWeeklyExp();
    }
}
