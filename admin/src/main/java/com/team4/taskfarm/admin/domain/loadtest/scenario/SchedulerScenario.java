package com.team4.taskfarm.admin.domain.loadtest.scenario;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("SCHEDULER")
@RequiredArgsConstructor
public class SchedulerScenario implements LoadScenario {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void runOnce() {
    Long userId = jdbcTemplate.queryForObject(
        "SELECT Idx_User FROM tbUser WHERE DeleteDate IS NULL AND Status = 'ACTIVE' ORDER BY Idx_User LIMIT 1",
        Long.class);

    jdbcTemplate.update(
        """
            INSERT INTO tbFarmEvent (Idx_User, EventKey, EventDate, IsDismissed, CreateDate)
            VALUES (?, 'LOAD_TEST', CURDATE(), 0, NOW())
            ON DUPLICATE KEY UPDATE IsDismissed = IsDismissed
            """,
        userId);
  }
}