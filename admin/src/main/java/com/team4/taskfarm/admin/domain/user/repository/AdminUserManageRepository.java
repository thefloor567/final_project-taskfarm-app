package com.team4.taskfarm.admin.domain.user.repository;

import com.team4.taskfarm.admin.domain.user.dto.UserListRow;
import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminUserManageRepository extends JpaRepository<TbUser, Long> {

       // 유저 목록 + 완료 할일 수 (소프트삭제 제외)
       @Query("SELECT u.idxUser as userId, u.nickname as nickname, u.email as email, " +
                     "u.level as level, u.status as status, u.createDate as joinDate, " +
                     "(SELECT COUNT(t) FROM TbTodo t WHERE t.idxUser = u.idxUser AND t.isDone = true AND t.deleteDate IS NULL) as doneCount "
                     +
                     "FROM TbUser u WHERE u.deleteDate IS NULL ORDER BY u.createDate DESC")
       List<UserListRow> findUserList();

       // 완료 할일 수
       @Query("SELECT COUNT(t) FROM TbTodo t WHERE t.idxUser = :userId AND t.isDone = true AND t.deleteDate IS NULL")
       long countDone(@Param("userId") Long userId);

       // 전체 할일 수 (완료율 분모)
       @Query("SELECT COUNT(t) FROM TbTodo t WHERE t.idxUser = :userId AND t.deleteDate IS NULL")
       long countTotal(@Param("userId") Long userId);

       // 수확 작물 수 (유저 농장의 수확 인벤토리 합)
       @Query("SELECT COALESCE(SUM(ci.qty), 0) FROM TbCropInv ci " +
                     "WHERE ci.idxFarm IN (SELECT f.idxFarm FROM TbFarm f WHERE f.idxUser = :userId)")
       long sumHarvestedCrops(@Param("userId") Long userId);

       // 최근 7일 일별 완료 할일 수
       @Query(value = "SELECT DATE(CompleteDate) as dt, COUNT(*) as cnt FROM tbTodo " +
                     "WHERE Idx_User = :userId AND IsDone = 1 AND DeleteDate IS NULL " +
                     "AND CompleteDate >= CURDATE() - INTERVAL 6 DAY " +
                     "GROUP BY DATE(CompleteDate) ORDER BY dt ASC", nativeQuery = true)
       List<Object[]> findRecentActivity(@Param("userId") Long userId);
}
