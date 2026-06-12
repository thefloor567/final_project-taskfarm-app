package com.team4.taskfarm.admin.domain.policy.service;

import com.team4.taskfarm.admin.domain.policy.dto.AchievementRequest;
import com.team4.taskfarm.admin.domain.policy.dto.AchievementResponse;
import com.team4.taskfarm.admin.domain.policy.repository.AchievementRepository;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementPolicyService {

    private final AchievementRepository achievementRepository;

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAll() {
        return achievementRepository.findAllByOrderByIdxAchievementAsc().stream()
                .map(AchievementResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AchievementResponse update(Long id, AchievementRequest request) {
        var achievement = achievementRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("업적을 찾을 수 없습니다."));
        achievement.updatePolicy(request.getCondValue(), request.getRewardCoin(), request.isActive());
        return new AchievementResponse(achievement);
    }
}
