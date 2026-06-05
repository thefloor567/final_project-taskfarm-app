package com.team4.taskfarm.admin.domain.auth.repository;

import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<TbUser, Long> {
    Optional<TbUser> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM TbUser u WHERE DATE(u.createDate) = CURRENT_DATE")
    long countTodaySignups();

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
