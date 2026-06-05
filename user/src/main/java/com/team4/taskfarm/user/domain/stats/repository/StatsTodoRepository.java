package com.team4.taskfarm.user.domain.stats.repository;

import com.team4.taskfarm.common.entity.todo.TbTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsTodoRepository extends JpaRepository<TbTodo, Long> {

    @Query("select count(t) from TbTodo t where t.idxUser = :idxUser and t.deleteDate is null")
    long countByIdxUserAndDeleteDateIsNull(@Param("idxUser") Long idxUser);

    @Query("select count(t) from TbTodo t where t.idxUser = :idxUser and t.isDone = true and t.deleteDate is null")
    long countByIdxUserAndIsDoneTrueAndDeleteDateIsNull(@Param("idxUser") Long idxUser);

    @Query("select count(t) from TbTodo t where t.idxUser = :idxUser and t.idxCat = :idxCat and t.deleteDate is null")
    long countByIdxUserAndIdxCatAndDeleteDateIsNull(@Param("idxUser") Long idxUser, @Param("idxCat") Long idxCat);

    @Query("select count(t) from TbTodo t where t.idxUser = :idxUser and t.idxCat = :idxCat and t.isDone = true and t.deleteDate is null")
    long countByIdxUserAndIdxCatAndIsDoneTrueAndDeleteDateIsNull(@Param("idxUser") Long idxUser, @Param("idxCat") Long idxCat);

    @Query("""
            select t
            from TbTodo t
            where t.idxUser = :idxUser
              and t.isDone = true
              and t.completeDate between :startDateTime and :endDateTime
              and t.deleteDate is null
            """)
    List<TbTodo> findByIdxUserAndIsDoneTrueAndCompleteDateBetweenAndDeleteDateIsNull(
            @Param("idxUser") Long idxUser,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}
