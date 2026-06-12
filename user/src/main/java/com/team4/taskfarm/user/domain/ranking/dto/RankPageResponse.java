package com.team4.taskfarm.user.domain.ranking.dto;

import java.util.List;

public record RankPageResponse(
        List<RankItemResponse> top,
        RankItemResponse me
) {
}