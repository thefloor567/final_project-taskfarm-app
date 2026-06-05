package com.team4.taskfarm.user.domain.category.repository;

import com.team4.taskfarm.user.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 유저의 삭제 안된 카테고리 목록
    List<Category> findByIdxUserAndDeleteDateIsNull(Long idxUser);
}