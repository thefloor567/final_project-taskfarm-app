package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.user.domain.achievement.service.AchievementService;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.farm.dto.OrderResponse;
import com.team4.taskfarm.user.domain.farm.dto.OrderResponse.OrderItemDto;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 주민 주문 (서버 권위). 경제 루프의 코인 획득 입구.
 * 레벨디자인 표4 + 의사코드 ④⑤⑥ 반영.
 *  - 생성: 현재 레벨 해금작물 중 랜덤, 수량 3~6, 보상 = Reward × 수량 × 1.5
 *  - 이행: 작물 차감 → 코인 지급(EARN, 원장 기록) → DONE → 빈 슬롯 새 주문 교체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final TbFarmRepository farmRepository;
    private final AuthUserRepository userRepository;
    private final TbSeedRepository seedRepository;
    private final TbCropInvRepository cropInvRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;
    private final TbOrderRepository orderRepository;
    private final TbOrderItemRepository orderItemRepository;
    private final AchievementService achievementService;

    // ── 레벨디자인 상수 (표4 / 의사코드) ──
    private static final int ORDER_SLOTS = 3;          // 동시 주문 슬롯
    private static final int QTY_MIN = 3, QTY_MAX = 6; // 요구 수량 범위
    private static final double REWARD_FACTOR = 1.5;   // 보상 계수

    /** 씨앗 해금 레벨 (의사코드 ④ SEED_UNLOCK). tbSeed 에 컬럼이 없어 코드 상수로 관리. */
    private static final Map<String, Integer> SEED_UNLOCK = Map.of(
            "radish", 1, "tomato", 1, "corn", 3, "pumpkin", 5
    );

    private static final String[] VILLAGERS = {"🐰 토끼", "🐻 곰", "🐱 고양이", "🦊 여우", "🐸 개구리"};

    /**
     * 주문 목록 조회. 슬롯이 비어 있으면 채워서 반환(처음 진입 시 자동 생성).
     */
    @Transactional
    public List<OrderResponse> getOrders(Long idxUser) {
        TbFarm farm = getFarm(idxUser);

        // OPEN 슬롯이 모자라면 새로 생성해 ORDER_SLOTS 채움
        List<TbOrder> open = orderRepository.findByIdxFarmAndState(farm.getIdxFarm(), TbOrder.State.OPEN);
        int level = currentLevel(idxUser);
        while (open.size() < ORDER_SLOTS) {
            open.add(generateOrder(farm.getIdxFarm(), level));
        }

        return toResponses(farm.getIdxFarm(), open);
    }

    /**
     * 주문 이행. 작물 차감 → 코인 지급(EARN) → DONE → 빈 슬롯에 새 주문 교체.
     * 전부 한 트랜잭션.
     */
    @Transactional
    public void fulfillOrder(Long idxUser, Long orderId) {
    	TbFarm farm = farmRepository.findByIdxUserForUpdate(idxUser)
    	        .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));

        TbOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.notFound("주문을 찾을 수 없습니다."));

        // 서버 권위: 내 농장 주문인지 검증
        if (!order.getIdxFarm().equals(farm.getIdxFarm())) {
            throw CustomException.forbidden("내 농장의 주문이 아닙니다.");
        }

        List<TbOrderItem> items = orderItemRepository.findByIdxOrder(orderId);
        if (items.isEmpty()) {
            throw CustomException.badRequest("주문 정보가 올바르지 않습니다.");
        }

        // ① 요구 작물 차감 (부족하면 엔티티에서 예외 → 롤백)
        for (TbOrderItem item : items) {
            TbCropInv inv = cropInvRepository
            		.findByIdxFarmAndIdxSeedForUpdate(farm.getIdxFarm(), item.getIdxSeed())
                    .orElseThrow(() -> CustomException.badRequest("작물이 부족합니다."));
            inv.consume(item.getQty());
        }

        // ② 코인 지급 + ③ 원장 EARN 기록 (한 트랜잭션)
        int reward = order.getReward();
        farm.earnCoin(reward);
        coinLedgerRepository.save(
                TbCoinLedger.earn(farm.getIdxFarm(), reward, "ORDER_FULFILL", order.getIdxOrder()));

        // ④ 주문 완료
        order.fulfill();

        // ⑤ 빈 슬롯에 새 주문 교체
        generateOrder(farm.getIdxFarm(), currentLevel(idxUser));

        // ⑥ 업적 체크 (본 기능 안 막게)
        try {
            achievementService.checkAndGrant(idxUser, "order_fulfill");
        } catch (Exception e) {
            log.warn("주문 업적 체크 실패(무시) - {}", e.getMessage());
        }
    }

    // ── 내부 ──

    /** 새 주문 생성 (의사코드 ⑤). 저장까지 하고 반환. */
    private TbOrder generateOrder(Long idxFarm, int level) {
        List<TbSeed> pool = unlockedSeeds(level);
        if (pool.isEmpty()) {
            // 해금 작물이 없을 일은 없지만 방어
            throw CustomException.badRequest("주문을 생성할 수 있는 작물이 없습니다.");
        }
        TbSeed seed = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        int qty = ThreadLocalRandom.current().nextInt(QTY_MIN, QTY_MAX + 1);
        int reward = (int) Math.round(seed.getReward() * qty * REWARD_FACTOR);
        String villager = VILLAGERS[ThreadLocalRandom.current().nextInt(VILLAGERS.length)];

        TbOrder order = orderRepository.save(TbOrder.create(idxFarm, villager, reward));
        orderItemRepository.save(TbOrderItem.create(order.getIdxOrder(), seed.getIdxSeed(), qty));
        return order;
    }

    /** 현재 레벨 해금 작물 (의사코드 ④ unlockedSeeds) */
    private List<TbSeed> unlockedSeeds(int level) {
        return seedRepository.findByIsActiveTrueOrderByIdxSeedAsc().stream()
                .filter(s -> {
                    Integer unlock = SEED_UNLOCK.get(s.getCode());
                    return unlock != null && unlock <= level;
                })
                .toList();
    }

    /** 현재 유저 레벨 읽기 (tbUser.Level). 한 곳에 격리 — 유저 연동 방식 바뀌면 여기만 수정. */
    private int currentLevel(Long idxUser) {
        return userRepository.findById(idxUser)
                .map(TbUser::getLevel)
                .orElse(1); // 유저 못 찾으면 최소 레벨로 방어
    }

    private TbFarm getFarm(Long idxUser) {
        return farmRepository.findByIdxUser(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));
    }

    /** 주문 + 요구작물 + 보유수량 → 응답 (N+1 방지 묶음 조회) */
    private List<OrderResponse> toResponses(Long idxFarm, List<TbOrder> orders) {
        if (orders.isEmpty()) return List.of();

        List<Long> orderIds = orders.stream().map(TbOrder::getIdxOrder).toList();
        Map<Long, List<TbOrderItem>> itemsByOrder = orderItemRepository.findByIdxOrderIn(orderIds)
                .stream().collect(Collectors.groupingBy(TbOrderItem::getIdxOrder));

        // 등장 작물 이름/코드 + 보유 수량 한 번에
        List<Long> seedIds = itemsByOrder.values().stream().flatMap(List::stream)
                .map(TbOrderItem::getIdxSeed).distinct().toList();
        Map<Long, TbSeed> seedById = seedRepository.findByIdxSeedIn(seedIds).stream()
                .collect(Collectors.toMap(TbSeed::getIdxSeed, s -> s));
        Map<Long, Integer> haveBySeed = cropInvRepository.findByIdxFarm(idxFarm).stream()
                .collect(Collectors.toMap(TbCropInv::getIdxSeed, TbCropInv::getQty, (a, b) -> a));

        List<OrderResponse> result = new ArrayList<>();
        for (TbOrder o : orders) {
            List<TbOrderItem> items = itemsByOrder.getOrDefault(o.getIdxOrder(), List.of());
            boolean canFulfill = !items.isEmpty();
            List<OrderItemDto> itemDtos = new ArrayList<>();
            for (TbOrderItem it : items) {
                TbSeed s = seedById.get(it.getIdxSeed());
                int have = haveBySeed.getOrDefault(it.getIdxSeed(), 0);
                if (have < it.getQty()) canFulfill = false;
                itemDtos.add(OrderItemDto.builder()
                        .seedId(it.getIdxSeed())
                        .cropName(s != null ? s.getName() : "작물")
                        .code(s != null ? s.getCode() : null)
                        .need(it.getQty())
                        .have(have)
                        .build());
            }
            result.add(OrderResponse.builder()
                    .orderId(o.getIdxOrder())
                    .villager(o.getVillager())
                    .reward(o.getReward())
                    .state(o.getState().name())
                    .canFulfill(canFulfill)
                    .items(itemDtos)
                    .build());
        }
        return result;
    }
}