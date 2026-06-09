package com.team4.taskfarm.admin.domain.stats.repository;

import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminStatsRepository extends JpaRepository<TbUser, Long> {

    @Query("SELECT COUNT(u) FROM TbUser u WHERE u.deleteDate IS NULL")
    long countTotalUsers();

    // 활성 유저: 최근 7일 내 할일을 완료한 distinct 유저
    @Query("SELECT COUNT(DISTINCT t.idxUser) FROM TbTodo t " +
           "WHERE t.isDone = true AND t.deleteDate IS NULL AND t.completeDate >= :since")
    long countActiveUsers(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM TbTodo t WHERE t.deleteDate IS NULL")
    long countTotalTodos();

    @Query("SELECT COUNT(t) FROM TbTodo t WHERE t.isDone = true AND t.deleteDate IS NULL")
    long countDoneTodos();

    // 월별 가입자 (최근 6개월)
    @Query(value = "SELECT DATE_FORMAT(CreateDate, '%Y-%m') as ym, COUNT(*) as cnt FROM tbUser " +
                   "WHERE DeleteDate IS NULL AND CreateDate >= DATE_FORMAT(CURDATE() - INTERVAL 5 MONTH, '%Y-%m-01') " +
                   "GROUP BY ym ORDER BY ym ASC", nativeQuery = true)
    List<Object[]> signupByMonth();

    // 레벨 분포 (한 행: g1,g2,g3,g4)
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN Level BETWEEN 1 AND 3 THEN 1 ELSE 0 END) as g1, " +
                   "SUM(CASE WHEN Level BETWEEN 4 AND 6 THEN 1 ELSE 0 END) as g2, " +
                   "SUM(CASE WHEN Level BETWEEN 7 AND 9 THEN 1 ELSE 0 END) as g3, " +
                   "SUM(CASE WHEN Level >= 10 THEN 1 ELSE 0 END) as g4 " +
                   "FROM tbUser WHERE DeleteDate IS NULL", nativeQuery = true)
    List<Object[]> levelDistribution();

    // 카테고리별 할일 수 (카테고리 없으면 '미분류')
    @Query(value = "SELECT COALESCE(c.Name, '미분류') as name, COUNT(t.Idx_Todo) as cnt " +
                   "FROM tbTodo t LEFT JOIN tbCategory c ON t.Idx_Cat = c.Idx_Cat " +
                   "WHERE t.DeleteDate IS NULL GROUP BY name ORDER BY cnt DESC", nativeQuery = true)
    List<Object[]> todoByCategory();

    // 요일별 완료 수 (MySQL DAYOFWEEK: 1=일 ~ 7=토)
    @Query(value = "SELECT DAYOFWEEK(CompleteDate) as dow, COUNT(*) as cnt FROM tbTodo " +
                   "WHERE IsDone = 1 AND DeleteDate IS NULL AND CompleteDate IS NOT NULL " +
                   "GROUP BY dow", nativeQuery = true)
    List<Object[]> completionByWeekday();
}
