package com.team4.taskfarm.admin.domain.ops.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.ops.dto.RankSnapshotResponse;
import com.team4.taskfarm.admin.domain.ops.service.RankAuditService;
import com.team4.taskfarm.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
public class RankAuditApiController extends AdminBaseController {

    private final RankAuditService rankAuditService;

    // GET /admin/api/ops/rank/snapshots?period=2026W24
    @GetMapping("/rank/snapshots")
    public ResponseEntity<ApiResponse<List<RankSnapshotResponse>>> getSnapshots(
            @RequestParam String period) {
        return ok(rankAuditService.getSnapshots(period));
    }
}
