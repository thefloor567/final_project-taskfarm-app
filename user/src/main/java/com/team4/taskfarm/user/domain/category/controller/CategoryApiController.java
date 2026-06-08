package com.team4.taskfarm.user.domain.category.controller;

import com.team4.taskfarm.user.domain.category.dto.CategoryCreateRequest;
import com.team4.taskfarm.user.domain.category.dto.CategoryUpdateRequest;
import com.team4.taskfarm.user.domain.category.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;

    // 목록 조회
    @GetMapping
    public Object getCategories() {
        // TODO: getCurrentUserIdx() → 공통모듈 완성 후 UserBaseController 상속으로 교체
        Long tempUserIdx = 2L; // 임시
        return categoryService.getCategories(tempUserIdx);
    }

    // 생성
    @PostMapping
    public Object createCategory(@Valid @RequestBody CategoryCreateRequest req) {
        Long tempUserIdx = 2L; // 임시
        return categoryService.createCategory(tempUserIdx, req);
    }

    // 수정
    @PutMapping("/{catIdx}")
    public Object updateCategory(@PathVariable Long catIdx,
                                  @RequestBody CategoryUpdateRequest req) {
        Long tempUserIdx = 2L; // 임시
        categoryService.updateCategory(tempUserIdx, catIdx, req);
        return "수정완료";
    }

    // 삭제
    @DeleteMapping("/{catIdx}")
    public Object deleteCategory(@PathVariable Long catIdx) {
        Long tempUserIdx = 2L; // 임시
        categoryService.deleteCategory(tempUserIdx, catIdx);
        return "삭제완료";
    }
}