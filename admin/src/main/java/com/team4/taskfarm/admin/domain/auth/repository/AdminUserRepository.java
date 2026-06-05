package com.team4.taskfarm.admin.domain.auth.repository;
 
import com.team4.taskfarm.common.entity.user.TbUser;  //
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
 
public interface AdminUserRepository extends JpaRepository<TbUser, Long> {
    Optional<TbUser> findByEmail(String email);
 
    @Query("SELECT COUNT(u) FROM TbUser u WHERE DATE(u.createDate) = CURRENT_DATE")
    long countTodaySignups();
}