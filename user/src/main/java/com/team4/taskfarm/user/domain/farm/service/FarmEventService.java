package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.TbEventConfig;
import com.team4.taskfarm.common.entity.farm.TbFarmEvent;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.user.domain.farm.repository.TbEventConfigRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmEventRepository;
import com.team4.taskfarm.user.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * 농장 이벤트 (서버 권위).
 *
 * 피드백 F(스케줄러 다중 Pod 중복 실행) 회피:
 *   - 스케줄러로 미리 만들지 않고, 농장 조회 시 "오늘 이벤트"를 lazy 결정.
 *   - 시드 = (유저ID + 날짜) → 같은 날 몇 번 호출해도 같은 결과(리롤 방지 = 서버 권위).
 *   - 이미 저장된 오늘 이벤트가 있으면 그대로 사용(스트릭이 도중에 바뀌어도 당일 고정).
 *   - UNIQUE(Idx_User, EventDate) 로 동시 요청 시 중복 INSERT 방어.
 *
 * 스트릭 풀버전:
 *   - tbEventConfig 에서 StreakMin <= 유저스트릭 인 활성 이벤트를 후보로,
 *     Weight 가중치 추첨. 스트릭 높을수록 좋은 이벤트(높은 StreakMin)가 후보에 추가됨.
 */
@Service
@RequiredArgsConstructor
public class FarmEventService {

    private final TbFarmEventRepository farmEventRepository;
    private final TbEventConfigRepository eventConfigRepository;
    private final UserRepository userRepository;

    /** 평범한 날(이벤트 설정이 하나도 없을 때의 안전 기본값) */
    private static final String DEFAULT_EVENT_KEY = "normal";

    /**
     * 오늘의 이벤트 조회 (없으면 시드로 생성).
     * getFarm 등에서 호출. 반환된 eventKey 로 화면 표시 / 효과 적용.
     */
    @Transactional
    public TbFarmEvent getTodayEvent(Long idxUser) {
        LocalDate today = LocalDate.now();

        // 1) 이미 오늘 이벤트가 있으면 그대로 (리롤 방지의 핵심)
        return farmEventRepository.findByIdxUserAndEventDate(idxUser, today)
                .orElseGet(() -> createTodayEvent(idxUser, today));
    }

    /** 오늘 이벤트 생성 (시드 고정 가중치 추첨) */
    private TbFarmEvent createTodayEvent(Long idxUser, LocalDate today) {
        int streak = userRepository.findById(idxUser).map(TbUser::getStreak).orElse(0);

        // 후보 풀: StreakMin <= 내 스트릭 인 활성 이벤트
        List<TbEventConfig> pool = eventConfigRepository
                .findByIsActiveTrueAndStreakMinLessThanEqual(streak);

        String eventKey = (pool.isEmpty())
                ? DEFAULT_EVENT_KEY
                : weightedPick(pool, seedOf(idxUser, today));

        try {
            return farmEventRepository.save(TbFarmEvent.create(idxUser, eventKey, today));
        } catch (DataIntegrityViolationException dup) {
            // 동시 요청으로 다른 트랜잭션이 먼저 INSERT한 경우 → 그 값을 읽어 반환
            return farmEventRepository.findByIdxUserAndEventDate(idxUser, today)
                    .orElseThrow(() -> dup);
        }
    }

    /**
     * 시드 고정 가중치 추첨.
     * 같은 (유저, 날짜, 풀) 이면 항상 같은 결과 → 리롤 불가.
     * 풀 순서가 흔들리면 결과가 바뀌므로, Idx 순으로 정렬 후 추첨.
     */
    private String weightedPick(List<TbEventConfig> pool, long seed) {
        List<TbEventConfig> sorted = pool.stream()
                .sorted((a, b) -> Long.compare(a.getIdxEventConfig(), b.getIdxEventConfig()))
                .toList();

        int totalWeight = sorted.stream().mapToInt(TbEventConfig::getWeight).sum();
        if (totalWeight <= 0) return DEFAULT_EVENT_KEY;

        // 시드 고정 난수 → 0..totalWeight-1
        int roll = new Random(seed).nextInt(totalWeight);

        int acc = 0;
        for (TbEventConfig cfg : sorted) {
            acc += cfg.getWeight();
            if (roll < acc) return cfg.getEventKey();
        }
        return sorted.get(sorted.size() - 1).getEventKey(); // 안전망
    }

    /** (유저ID + 날짜) → 고정 시드 */
    private long seedOf(Long idxUser, LocalDate date) {
        return idxUser * 1_000_000L + date.toEpochDay();
    }
}