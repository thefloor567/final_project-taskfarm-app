package com.team4.taskfarm.admin.domain.user.service;

import com.team4.taskfarm.admin.domain.user.dto.UserDetailResponse;
import com.team4.taskfarm.admin.domain.user.dto.UserListResponse;
import com.team4.taskfarm.admin.domain.user.repository.AdminUserManageRepository;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserManageRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserListResponse> getUserList() {
        return userRepository.findUserList().stream()
                .map(UserListResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        long doneCount = userRepository.countDone(userId);
        long totalCount = userRepository.countTotal(userId);
        double rate = totalCount > 0
                ? Math.round((double) doneCount / totalCount * 1000.0) / 10.0
                : 0.0;
        long cropCount = userRepository.sumHarvestedCrops(userId);

        // 최근 7일 완료 추이 (빈 날짜는 0으로 채움)
        Map<String, Long> byDate = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (Object[] row : userRepository.findRecentActivity(userId)) {
            String date = row[0].toString().substring(5, 10); // yyyy-MM-dd → MM-dd
            byDate.put(date, ((Number) row[1]).longValue());
        }
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String label = today.minusDays(i).format(fmt);
            labels.add(label);
            data.add(byDate.getOrDefault(label, 0L));
        }

        return new UserDetailResponse(user, doneCount, rate, cropCount, labels, data);
    }
}
