package com.team4.taskfarm.admin.domain.policy.service;

import com.team4.taskfarm.admin.domain.policy.dto.SeedPolicyResponse;
import com.team4.taskfarm.admin.domain.policy.dto.ShopPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.dto.ToolPolicyResponse;
import com.team4.taskfarm.admin.domain.policy.repository.SeedRepository;
import com.team4.taskfarm.admin.domain.policy.repository.ToolRepository;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopPolicyService {

    private final SeedRepository seedRepository;
    private final ToolRepository toolRepository;

    @Transactional(readOnly = true)
    public List<SeedPolicyResponse> getSeeds() {
        return seedRepository.findAll().stream()
                .map(SeedPolicyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ToolPolicyResponse> getTools() {
        return toolRepository.findAll().stream()
                .map(ToolPolicyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeedPolicyResponse updateSeed(Long id, ShopPolicyRequest request) {
        var seed = seedRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("씨앗을 찾을 수 없습니다."));
        seed.updatePolicy(request.getPrice(), request.getReward(), request.getStock(), request.getDailyLimit());
        return new SeedPolicyResponse(seed);
    }

    @Transactional
    public ToolPolicyResponse updateTool(Long id, ShopPolicyRequest request) {
        var tool = toolRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("도구를 찾을 수 없습니다."));
        // 도구는 reward 자리에 효과수치(uses)가 들어옴
        tool.updatePolicy(request.getPrice(), request.getReward(), request.getStock(), request.getDailyLimit());
        return new ToolPolicyResponse(tool);
    }
}
