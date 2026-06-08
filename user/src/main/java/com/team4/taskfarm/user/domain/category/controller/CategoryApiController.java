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

    /*
     * 테스트용 유저 번호.
     *
     * 현재 JWT/Auth 공통모듈이 완전히 연결되지 않아 getCurrentUserIdx()가 null이 될 수 있음.
     * 그래서 로컬 테스트 중에는 실제 DB에 존재하는 Idx_User 값을 임시로 사용한다.
     *
     * 예)
     * SELECT Idx_User, Email FROM tbUser;
     *
     * 위 쿼리로 테스트할 유저의 Idx_User를 확인한 뒤 아래 값을 바꿔서 사용.
     *
     * ⚠ PR 올리기 전 또는 JWT 연동 후에는 반드시 getCurrentUserIdx()로 되돌릴 것.
     */
    private static final Long TEST_USER_IDX = 8L;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // return ok(categoryService.getCategories(getCurrentUserIdx()));

        // 테스트용: TEST_USER_IDX에 해당하는 유저의 카테고리 목록 조회
        return ok(categoryService.getCategories(TEST_USER_IDX));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest req) {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // return ok(categoryService.createCategory(getCurrentUserIdx(), req));

        // 테스트용: TEST_USER_IDX에 해당하는 유저의 카테고리 생성
        return ok(categoryService.createCategory(TEST_USER_IDX, req));
    }

    @PutMapping("/{catIdx}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long catIdx,
            @RequestBody CategoryUpdateRequest req) {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // categoryService.updateCategory(getCurrentUserIdx(), catIdx, req);

        // 테스트용: TEST_USER_IDX에 해당하는 유저 기준으로 카테고리 수정
        categoryService.updateCategory(TEST_USER_IDX, catIdx, req);
        return ok();
    }

    @DeleteMapping("/{catIdx}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long catIdx) {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // categoryService.deleteCategory(getCurrentUserIdx(), catIdx);

        // 테스트용: TEST_USER_IDX에 해당하는 유저 기준으로 카테고리 삭제
        categoryService.deleteCategory(TEST_USER_IDX, catIdx);
        return ok();
    }
    
}