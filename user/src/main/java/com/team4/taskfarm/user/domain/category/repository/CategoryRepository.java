package com.team4.taskfarm.user.domain.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.user.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 유저의 삭제 안된 카테고리 목록
    List<Category> findByIdxUserAndDeleteDateIsNull(Long idxUser);
    
    // 단건 조회 메서드
    Optional<Category> findByIdxCatAndIdxUserAndDeleteDateIsNull(Long idxCat, Long idxUser);
}