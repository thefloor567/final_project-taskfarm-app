package com.team4.taskfarm.admin.domain.stats.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.stats.dto.StatsResponse;
import com.team4.taskfarm.admin.domain.stats.service.AdminStatsService;
import com.team4.taskfarm.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class AdminStatsApiController extends AdminBaseController {

    private final AdminStatsService adminStatsService;

    // GET /admin/api/stats
    @GetMapping
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        return ok(adminStatsService.getStats());
    }
}
