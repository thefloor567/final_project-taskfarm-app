package com.team4.taskfarm.user.domain.stats.repository;

import com.team4.taskfarm.common.entity.todo.TbTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsTodoRepository extends JpaRepository<TbTodo, Long> {

    @Query("select count(t) from TbTodo t where t.idxUser = :idxUser and t.deleteDate is null")
    long countTodos(@Param("idxUser") Long idxUser);

    @Query("select count(t) from TbTodo t where t.idxUser = :idxUser and t.isDone = true and t.deleteDate is null")
    long countCompletedTodos(@Param("idxUser") Long idxUser);

    @Query("""
            select t.idxCat as idxCat,
                   count(t) as totalCount,
                   sum(case when t.isDone = true then 1 else 0 end) as doneCount
            from TbTodo t
            where t.idxUser = :idxUser
              and t.idxCat is not null
              and t.deleteDate is null
            group by t.idxCat
            """)
    List<CategoryCompletionStat> findCategoryCompletionStats(@Param("idxUser") Long idxUser);

    @Query("""
            select t
            from TbTodo t
            where t.idxUser = :idxUser
              and t.isDone = true
              and t.completeDate between :startDateTime and :endDateTime
              and t.deleteDate is null
            """)
    List<TbTodo> findCompletedBetween(
            @Param("idxUser") Long idxUser,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    interface CategoryCompletionStat {
        Long getIdxCat();

        Long getTotalCount();

        Long getDoneCount();
    }
}
