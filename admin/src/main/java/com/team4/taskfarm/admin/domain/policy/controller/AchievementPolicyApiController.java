package com.team4.taskfarm.admin.domain.policy.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.policy.dto.AchievementRequest;
import com.team4.taskfarm.admin.domain.policy.dto.AchievementResponse;
import com.team4.taskfarm.admin.domain.policy.service.AchievementPolicyService;
import com.team4.taskfarm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies/achievements")
@RequiredArgsConstructor
public class AchievementPolicyApiController extends AdminBaseController {

    private final AchievementPolicyService achievementPolicyService;

    // GET /admin/api/policies/achievements
    @GetMapping
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAll() {
        return ok(achievementPolicyService.getAll());
    }

    // PATCH /admin/api/policies/achievements/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AchievementResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AchievementRequest request) {
        return ok(achievementPolicyService.update(id, request));
    }
}
