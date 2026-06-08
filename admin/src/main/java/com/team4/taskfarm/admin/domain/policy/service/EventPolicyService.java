package com.team4.taskfarm.admin.domain.policy.service;

import com.team4.taskfarm.admin.domain.policy.dto.EventConfigRequest;
import com.team4.taskfarm.admin.domain.policy.dto.EventConfigResponse;
import com.team4.taskfarm.admin.domain.policy.repository.EventConfigRepository;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventPolicyService {

    private final EventConfigRepository eventConfigRepository;

    @Transactional(readOnly = true)
    public List<EventConfigResponse> getAll() {
        return eventConfigRepository.findAllByOrderByStreakMinAscIdxEventConfigAsc().stream()
                .map(EventConfigResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventConfigResponse update(Long id, EventConfigRequest request) {
        var config = eventConfigRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("이벤트 정책을 찾을 수 없습니다."));
        config.updatePolicy(request.getWeight(), request.isActive());
        return new EventConfigResponse(config);
    }
}
