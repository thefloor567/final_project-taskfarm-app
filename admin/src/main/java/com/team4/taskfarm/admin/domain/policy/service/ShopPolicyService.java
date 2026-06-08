package com.team4.taskfarm.admin.domain.policy.service;

import com.team4.taskfarm.admin.domain.policy.dto.SeedPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.dto.SeedPolicyResponse;
import com.team4.taskfarm.admin.domain.policy.dto.ToolPolicyRequest;
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
        return seedRepository.findAllByOrderByIdxSeedAsc().stream()
                .map(SeedPolicyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ToolPolicyResponse> getTools() {
        return toolRepository.findAllByOrderByIdxToolAsc().stream()
                .map(ToolPolicyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeedPolicyResponse updateSeed(Long id, SeedPolicyRequest request) {
        var seed = seedRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("씨앗을 찾을 수 없습니다."));
        seed.updatePolicy(request.getPrice(), request.getReward(), request.getStock(), request.getDailyLimit());
        return new SeedPolicyResponse(seed);
    }

    @Transactional
    public ToolPolicyResponse updateTool(Long id, ToolPolicyRequest request) {
        var tool = toolRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("도구를 찾을 수 없습니다."));
        tool.updatePolicy(request.getPrice(), request.getUses(), request.getStock(), request.getDailyLimit());
        return new ToolPolicyResponse(tool);
    }
}
