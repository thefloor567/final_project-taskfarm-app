package com.team4.taskfarm.user.domain.farm.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.farm.dto.PlotShopItemResponse;
import com.team4.taskfarm.user.domain.farm.service.PlotShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/plots")
@RequiredArgsConstructor
public class PlotShopController extends UserBaseController {

    private final PlotShopService plotShopService;

    /** 밭 상점 진열 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlotShopItemResponse>>> getPlotShop() {
        return ok(plotShopService.getPlotShop(getCurrentUserIdx()));
    }

    /** 밭 구매 */
    @PostMapping("/{slot}/buy")
    public ResponseEntity<ApiResponse<Void>> buyPlot(@PathVariable int slot) {
        plotShopService.buyPlot(getCurrentUserIdx(), slot);
        return ok();
    }
}