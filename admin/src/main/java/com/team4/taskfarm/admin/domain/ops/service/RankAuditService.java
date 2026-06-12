package com.team4.taskfarm.admin.domain.ops.service;

import com.team4.taskfarm.admin.domain.ops.dto.RankSnapshotResponse;
import com.team4.taskfarm.admin.domain.ops.repository.RankSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankAuditService {

    private final RankSnapshotRepository rankSnapshotRepository;

    @Transactional(readOnly = true)
    public List<RankSnapshotResponse> getSnapshots(String period) {
        return rankSnapshotRepository.findSnapshot(period).stream()
                .map(RankSnapshotResponse::new)
                .collect(Collectors.toList());
    }
}
