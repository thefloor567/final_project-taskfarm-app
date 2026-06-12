package com.team4.taskfarm.admin.domain.ops.repository;

import com.team4.taskfarm.admin.domain.ops.dto.MailHistoryRow;
import com.team4.taskfarm.common.entity.social.TbMail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminMailRepository extends JpaRepository<TbMail, Long> {

    // 발송 대상: 활성 유저 전체 ID (소프트삭제 제외)
    @Query(value = "SELECT Idx_User FROM tbUser WHERE DeleteDate IS NULL", nativeQuery = true)
    List<Long> findAllActiveUserIds();

    // 멱등 처리용: 해당 RefKey로 이미 우편 받은 유저 ID
    @Query("SELECT m.idxUser FROM TbMail m WHERE m.refKey = :refKey")
    List<Long> findUserIdsByRefKey(@Param("refKey") String refKey);

    // 발송 이력 (어드민 SYSTEM 발송만, RefKey별 그룹)
    @Query("SELECT m.refKey as refKey, m.title as title, COUNT(m) as sentCount, " +
           "m.rewardType as rewardType, m.rewardCoin as rewardCoin, m.rewardTitle as rewardTitle, " +
           "MAX(m.createDate) as createDate " +
           "FROM TbMail m WHERE m.source = 'SYSTEM' " +
           "GROUP BY m.refKey, m.title, m.rewardType, m.rewardCoin, m.rewardTitle " +
           "ORDER BY MAX(m.createDate) DESC")
    List<MailHistoryRow> findHistory(Pageable pageable);
}
