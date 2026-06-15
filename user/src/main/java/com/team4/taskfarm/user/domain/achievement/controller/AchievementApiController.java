package com.team4.taskfarm.user.domain.achievement.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.achievement.dto.AchievementListResponse;
import com.team4.taskfarm.user.domain.achievement.dto.TitleEquipRequest;
import com.team4.taskfarm.user.domain.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementApiController extends UserBaseController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<ApiResponse<AchievementListResponse>> getAchievements() {
        return ok(achievementService.getAchievements(getCurrentUserIdx()));
    }

    @PutMapping("/title")
    public ResponseEntity<ApiResponse<Void>> equipTitle(@RequestBody TitleEquipRequest req) {
        achievementService.equipTitle(getCurrentUserIdx(), req.getTitle());
        return ok();
    }
}