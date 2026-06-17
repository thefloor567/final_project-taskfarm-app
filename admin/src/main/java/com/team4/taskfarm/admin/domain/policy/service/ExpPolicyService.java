package com.team4.taskfarm.admin.domain.policy.service;

import com.team4.taskfarm.admin.domain.policy.dto.ExpPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.dto.ExpPolicyResponse;
import com.team4.taskfarm.admin.domain.policy.repository.ExpPolicyRepository;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpPolicyService {

    private final ExpPolicyRepository expPolicyRepository;

    @Transactional(readOnly = true)
    public List<ExpPolicyResponse> getAll() {
        return expPolicyRepository.findAllByOrderByPriorityAsc().stream()
                .map(ExpPolicyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpPolicyResponse update(Long id, ExpPolicyRequest request) {
        var policy = expPolicyRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("정책을 찾을 수 없습니다."));
        policy.update(request.getBaseExp(), request.getMinExp(), request.getMaxExp(),
                request.getDoneDrops(), request.getLevelUpDrops());
        return new ExpPolicyResponse(policy);
    }
}
