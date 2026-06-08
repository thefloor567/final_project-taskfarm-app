package com.team4.taskfarm.user.domain.category.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.category.dto.CategoryCreateRequest;
import com.team4.taskfarm.user.domain.category.dto.CategoryResponse;
import com.team4.taskfarm.user.domain.category.dto.CategoryUpdateRequest;
import com.team4.taskfarm.user.domain.category.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryApiController extends UserBaseController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ok(categoryService.getCategories(getCurrentUserIdx()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest req) {
        return ok(categoryService.createCategory(getCurrentUserIdx(), req));
    }

    @PutMapping("/{catIdx}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long catIdx,
            @RequestBody CategoryUpdateRequest req) {
        categoryService.updateCategory(getCurrentUserIdx(), catIdx, req);
        return ok();
    }

    @DeleteMapping("/{catIdx}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long catIdx) {
        categoryService.deleteCategory(getCurrentUserIdx(), catIdx);
        return ok();
    }
    
}