package com.team4.taskfarm.user.domain.farm.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 농장 관련 뷰 (농장/씨앗상점/도구상점/인벤토리/주민주문).
 * API는 같은 도메인의 FarmApiController(@RestController, /api/farm/**)가 담당.
 */
@Controller
public class FarmController extends UserBaseController {

    // 농장 메인
    @GetMapping("/farm")
    public String farm() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "farm/farm";
    }

    // 씨앗 상점
    @GetMapping("/farm/shop/seeds")
    public String seedShop() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "farm/seed-shop";
    }

    // 도구 상점
    @GetMapping("/farm/shop/tools")
    public String toolShop() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "farm/tool-shop";
    }
    
    // 밭 상점
    @GetMapping("/farm/shop/plots")
    public String plotShop() {
    	return "farm/plot-shop";
    }

    // 인벤토리 (보유 씨앗 / 수확 작물)
    @GetMapping("/farm/inventory")
    public String inventory() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "farm/inventory";
    }

    // 주민 주문
    @GetMapping("/farm/orders")
    public String orders() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "farm/orders";
    }

}