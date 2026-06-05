package com.team4.taskfarm.user.domain.stats.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.stats.dto.StatsDashboardResponse;
import com.team4.taskfarm.user.domain.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsApiController extends UserBaseController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<ApiResponse<StatsDashboardResponse>> getStats() {
        Long idxUser = getCurrentUserIdx();
        if (idxUser == null) {
            return fail("로그인이 필요합니다.");
        }
        return ok(statsService.getDashboard(idxUser));
    }
}
