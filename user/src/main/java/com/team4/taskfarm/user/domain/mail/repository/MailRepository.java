package com.team4.taskfarm.user.domain.mail.repository;

import com.team4.taskfarm.common.entity.social.TbMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MailRepository extends JpaRepository<TbMail, Long> {

    List<TbMail> findByIdxUserOrderByCreateDateDesc(Long idxUser);

    Optional<TbMail> findByIdxMailAndIdxUser(Long idxMail, Long idxUser);

    @Query("""
            select count(m)
            from TbMail m
            where m.idxUser = :idxUser
              and m.isClaimed = false
              and (m.expireAt is null or m.expireAt > :now)
            """)
    long countUnread(@Param("idxUser") Long idxUser, @Param("now") LocalDateTime now);

    @Query("""
            select m
            from TbMail m
            where m.idxUser = :idxUser
              and m.isClaimed = false
              and (m.expireAt is null or m.expireAt > :now)
            order by m.createDate desc
            """)
    List<TbMail> findClaimableMails(@Param("idxUser") Long idxUser, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update TbMail m
            set m.isClaimed = true,
                m.claimDate = :claimDate
            where m.idxMail = :idxMail
              and m.idxUser = :idxUser
              and m.isClaimed = false
              and (m.expireAt is null or m.expireAt > :claimDate)
            """)
    int claimIfUnclaimed(
            @Param("idxMail") Long idxMail,
            @Param("idxUser") Long idxUser,
            @Param("claimDate") LocalDateTime claimDate
    );
}
