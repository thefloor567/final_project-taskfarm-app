package com.team4.taskfarm.user.domain.farm.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.farm.dto.FarmResponse;
import com.team4.taskfarm.user.domain.farm.dto.PlantRequest;
import com.team4.taskfarm.user.domain.farm.dto.SeedInvResponse;
import com.team4.taskfarm.user.domain.farm.service.FarmCultivationService;
import com.team4.taskfarm.user.domain.farm.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farm")
@RequiredArgsConstructor
public class FarmApiController extends UserBaseController {

    private final FarmService farmService;
    private final FarmCultivationService cultivationService;

    /** 농장 전체 스냅샷 */
    @GetMapping
    public ResponseEntity<ApiResponse<FarmResponse>> getFarm() {
        return ok(farmService.getFarm(uid()));
    }

    /** 보유 씨앗 목록 (심을 씨앗 고르기) */
    @GetMapping("/inventory/seeds")
    public ResponseEntity<ApiResponse<List<SeedInvResponse>>> getSeedInventoryList() {
        return ok(cultivationService.getSeedInventoryList(uid()));
    }

    /** 씨앗 심기 */
    @PostMapping("/plots/{plotId}/plant")
    public ResponseEntity<ApiResponse<Void>> plantSeed(
            @PathVariable Long plotId,
            @Valid @RequestBody PlantRequest req) {
        cultivationService.plantSeed(uid(), plotId, req.getSeedId());
        return ok();
    }

    /** 물주기 */
    @PostMapping("/plots/{plotId}/water")
    public ResponseEntity<ApiResponse<Void>> waterPlot(@PathVariable Long plotId) {
        cultivationService.waterPlot(uid(), plotId);
        return ok();
    }

    /** 수확 */
    @PostMapping("/plots/{plotId}/harvest")
    public ResponseEntity<ApiResponse<Void>> harvestPlot(@PathVariable Long plotId) {
        cultivationService.harvestPlot(uid(), plotId);
        return ok();
    }

    /** 시든 작물 제거 */
    @DeleteMapping("/plots/{plotId}/crop")
    public ResponseEntity<ApiResponse<Void>> removeCrop(@PathVariable Long plotId) {
        cultivationService.removeCrop(uid(), plotId);
        return ok();
    }
    
    private Long uid() {
        Long id = getCurrentUserIdx();
        return id != null ? id : 1L;   // 인증 전이면 테스트 유저 1번
    }
}