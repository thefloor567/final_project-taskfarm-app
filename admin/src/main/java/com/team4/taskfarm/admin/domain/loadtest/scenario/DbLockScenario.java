package com.team4.taskfarm.admin.domain.loadtest.scenario;

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
    Long farmId = jdbcTemplate.queryForObject(
        "SELECT Idx_Farm FROM tbFarm ORDER BY Idx_Farm LIMIT 1",
        Long.class);

    jdbcTemplate.queryForObject(
        "SELECT Coin FROM tbFarm WHERE Idx_Farm = ? FOR UPDATE",
        Integer.class,
        farmId);

    Thread.sleep(50);

    jdbcTemplate.update(
        "UPDATE tbFarm SET Coin = Coin WHERE Idx_Farm = ?",
        farmId);
  }
}