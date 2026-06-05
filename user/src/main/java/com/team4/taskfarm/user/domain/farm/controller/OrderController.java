package com.team4.taskfarm.user.domain.farm.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.farm.dto.OrderResponse;
import com.team4.taskfarm.user.domain.farm.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController extends UserBaseController {

    private final OrderService orderService;

    /** 주민 주문 목록 (슬롯 비면 자동 채움) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders() {
        return ok(orderService.getOrders(uid()));
    }

    /** 주문 이행 (작물 납품 → 코인 획득) */
    @PostMapping("/{orderId}/fulfill")
    public ResponseEntity<ApiResponse<Void>> fulfillOrder(@PathVariable Long orderId) {
        orderService.fulfillOrder(uid(), orderId);
        return ok();
    }
    
    private Long uid() {
        Long id = getCurrentUserIdx();
        return id != null ? id : 1L;   // 인증 전이면 테스트 유저 1번
    }
}