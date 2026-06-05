package com.team4.taskfarm.user.domain.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.team4.taskfarm.user.domain.category.dto.CategoryCreateRequest;
import com.team4.taskfarm.user.domain.category.dto.CategoryResponse;
import com.team4.taskfarm.user.domain.category.dto.CategoryUpdateRequest;
import com.team4.taskfarm.user.domain.category.entity.Category;
import com.team4.taskfarm.user.domain.category.repository.CategoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 목록 조회
    @Transactional
    public List<CategoryResponse> getCategories(Long userIdx) {
        return categoryRepository.findByIdxUserAndDeleteDateIsNull(userIdx)
            .stream()
            .map(CategoryResponse::from)
            .collect(Collectors.toList());
    }

    // 카테고리 생성
    @Transactional
    public CategoryResponse createCategory(Long userIdx, CategoryCreateRequest req) {
        Category category = Category.create(userIdx, req.getName(), req.getColor());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    // 카테고리 수정
    @Transactional
    public void updateCategory(Long userIdx, Long catIdx, CategoryUpdateRequest req) {
        Category category = categoryRepository.findById(catIdx)
            .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        if (!category.getIdxUser().equals(userIdx)) {
            throw new RuntimeException("본인 카테고리만 수정할 수 있습니다.");
        }
        category.update(req.getName(), req.getColor());
    }

    // 카테고리 삭제
    @Transactional
    public void deleteCategory(Long userIdx, Long catIdx) {
        Category category = categoryRepository.findById(catIdx)
            .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        if (!category.getIdxUser().equals(userIdx)) {
            throw new RuntimeException("본인 카테고리만 삭제할 수 있습니다.");
        }
        category.delete();
    }
}