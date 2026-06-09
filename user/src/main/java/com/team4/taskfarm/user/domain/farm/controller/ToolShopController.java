package com.team4.taskfarm.user.domain.farm.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.farm.dto.BuyToolRequest;
import com.team4.taskfarm.user.domain.farm.dto.ToolShopItemResponse;
import com.team4.taskfarm.user.domain.farm.service.ToolShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/tools")
@RequiredArgsConstructor
public class ToolShopController extends UserBaseController {

    private final ToolShopService toolShopService;

    /** 도구 진열 목록 (잠김 표시 포함) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ToolShopItemResponse>>> getToolShopList() {
        return ok(toolShopService.getToolShopList(getCurrentUserIdx()));
    }

    /** 도구 구매 + 즉시 효과. 비료는 body.targetPlotId 필요. */
    @PostMapping("/{toolId}/buy")
    public ResponseEntity<ApiResponse<Void>> buyTool(
            @PathVariable Long toolId,
            @RequestBody(required = false) BuyToolRequest req) {
        Long targetPlotId = (req != null) ? req.getTargetPlotId() : null;
        toolShopService.buyTool(getCurrentUserIdx(), toolId, targetPlotId);
        return ok();
    }
}