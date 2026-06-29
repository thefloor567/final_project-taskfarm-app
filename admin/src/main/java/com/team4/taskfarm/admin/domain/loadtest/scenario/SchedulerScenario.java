package com.team4.taskfarm.admin.domain.loadtest.scenario;

import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("SCHEDULER")
@RequiredArgsConstructor
public class SchedulerScenario implements LoadScenario {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void runOnce() {
        Long userId = findAnyUserId();

        try {
            jdbcTemplate.update(
                    """
                            INSERT INTO tbFarmEvent (Idx_User, EventKey, EventDate, IsDismissed, CreateDate)
                            VALUES (?, 'LOAD_TEST', CURDATE(), 0, NOW())
                            ON DUPLICATE KEY UPDATE IsDismissed = IsDismissed
                            """,
                    userId);
        } catch (Exception e) {
            throw CustomException.badRequest("SCHEDULER 시나리오 실행 중 오류가 발생했습니다.");
        }
    }

    private Long findAnyUserId() {
        Long userId = jdbcTemplate.query(
                "SELECT Idx_User FROM tbUser WHERE DeleteDate IS NULL AND Status = 'ACTIVE' ORDER BY Idx_User LIMIT 1",
                rs -> rs.next() ? rs.getLong(1) : null);

        if (userId == null) {
            throw CustomException.badRequest("SCHEDULER 시나리오 실행용 활성 유저를 찾을 수 없습니다.");
        }

        return userId;
    }
}