package com.team4.taskfarm.user.domain.stats.repository;

import com.team4.taskfarm.common.entity.category.TbCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatsCategoryRepository extends JpaRepository<TbCategory, Long> {

    List<TbCategory> findByIdxUserAndDeleteDateIsNullOrderByCreateDateAsc(Long idxUser);
}
