package com.team4.taskfarm.admin.domain.loadtest.scenario;

import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("DB_LOCK")
@RequiredArgsConstructor
public class DbLockScenario implements LoadScenario {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void runOnce() throws Exception {
        Long farmId = findAnyFarmId();

        try {
            jdbcTemplate.queryForObject(
                    "SELECT Coin FROM tbFarm WHERE Idx_Farm = ? FOR UPDATE",
                    Integer.class,
                    farmId);

            Thread.sleep(50);

            jdbcTemplate.update(
                    "UPDATE tbFarm SET Coin = Coin WHERE Idx_Farm = ?",
                    farmId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CustomException.badRequest("DB_LOCK 시나리오 실행이 중단되었습니다.");
        } catch (Exception e) {
            throw CustomException.badRequest("DB_LOCK 시나리오 실행 중 오류가 발생했습니다.");
        }
    }

    private Long findAnyFarmId() {
        Long farmId = jdbcTemplate.query(
                "SELECT Idx_Farm FROM tbFarm ORDER BY Idx_Farm LIMIT 1",
                rs -> rs.next() ? rs.getLong(1) : null);

        if (farmId == null) {
            throw CustomException.badRequest("DB_LOCK 시나리오 실행용 Farm 데이터를 찾을 수 없습니다.");
        }

        return farmId;
    }
}