package com.team4.taskfarm.user.domain.farm.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.farm.dto.BuySeedRequest;
import com.team4.taskfarm.user.domain.farm.dto.SeedShopItemResponse;
import com.team4.taskfarm.user.domain.farm.service.SeedShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/seeds")
@RequiredArgsConstructor
public class SeedShopController extends UserBaseController {

    private final SeedShopService seedShopService;

    /** 상점 진열 목록 (판매중 씨앗) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SeedShopItemResponse>>> getShopItems() {
        return ok(seedShopService.getShopItems());
    }

    /** 씨앗 구매 */
    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<Void>> buySeed(@Valid @RequestBody BuySeedRequest req) {
        seedShopService.buySeed(getCurrentUserIdx(), req.getSeedId(), req.getQty());
        return ok();
    }
}