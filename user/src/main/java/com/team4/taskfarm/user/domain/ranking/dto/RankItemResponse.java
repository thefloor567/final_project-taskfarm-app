package com.team4.taskfarm.user.domain.ranking.dto;

// social.html 랭킹 리스트 한 줄에 필요한 응답 DTO.
public record RankItemResponse(
        int rank,
        Long userId,
        String nickname,
        String title,
        int exp
) {
}