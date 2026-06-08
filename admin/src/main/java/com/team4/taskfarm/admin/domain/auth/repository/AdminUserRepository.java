package com.team4.taskfarm.admin.domain.auth.repository;

import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<TbUser, Long> {
    Optional<TbUser> findByEmail(String email);

    // 오늘 가입자: 범위조건(인덱스 활용) + 소프트삭제 제외
    @Query("SELECT COUNT(u) FROM TbUser u " +
           "WHERE u.createDate >= :start AND u.createDate < :end AND u.deleteDate IS NULL")
    long countTodaySignups(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT DATE(u.CreateDate) as dt, COUNT(*) as cnt
        FROM tbUser u
        WHERE u.CreateDate >= CURDATE() - INTERVAL 6 DAY
          AND u.DeleteDate IS NULL
        GROUP BY DATE(u.CreateDate)
        ORDER BY dt ASC
        """, nativeQuery = true)
    List<Object[]> countSignupsLast7Days();
}
