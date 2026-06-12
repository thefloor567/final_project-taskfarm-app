package com.team4.taskfarm.user.domain.ranking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.ranking.dto.RankPageResponse;
import com.team4.taskfarm.user.domain.ranking.service.RankingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RankApiController extends UserBaseController {

    private final RankingService rankingService;

    @GetMapping("/api/rank")
    public ResponseEntity<ApiResponse<RankPageResponse>> getRank(
            @RequestParam(defaultValue = "total") String type,
            @RequestParam(defaultValue = "all") String scope
    ) {
        Long idxUser = getCurrentUserIdx();

        RankPageResponse response = rankingService.getRankPage(type, scope, idxUser);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}