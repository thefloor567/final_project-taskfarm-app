package com.team4.taskfarm.common.controller;

import com.team4.taskfarm.common.dto.SearchDto;
import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.common.response.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 모든 컨트롤러의 공통 베이스 (앱 무관).
 * - SecurityContext 기반 유저 정보, 권한, 페이지네이션, 공통 응답, 로깅.
 * - 특정 앱(User 엔티티 등)에 의존하지 않음. DB 조회가 필요한 공통은
 *   각 앱의 UserBaseController/AdminBaseController에서 추가.
 */
@Slf4j
public abstract class BaseController {

    protected static final int ROW_CNT = 5;

    @Autowired
    protected HttpServletRequest request;

    // ───────── 유저 정보 (SecurityContext only, DB 조회 없음) ─────────

    /** 현재 로그인 유저 식별자(subject = email) */
    protected String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    /** JWT credentials에 담긴 userIdx (DB 조회 X) */
    protected Long getCurrentUserIdx() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object cred = auth.getCredentials();
        return (cred instanceof Long) ? (Long) cred : null;
    }

    /** ROLE_ADMIN 여부 */
    protected boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /** 로그인 여부 */
    protected boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    // ───────── IP ─────────

    /** 클라이언트 IP (프록시/ALB 환경 대응) */
    protected String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }

    // ───────── 페이지네이션 ─────────

    protected Pageable getPageable(int page) {
        return PageRequest.of(Math.max(page - 1, 0), ROW_CNT,
                Sort.by(Sort.Direction.DESC, "createDate"));
    }

    protected Pageable getPageable(int page, String sortField, Sort.Direction direction) {
        return PageRequest.of(Math.max(page - 1, 0), ROW_CNT, Sort.by(direction, sortField));
    }

    protected Pageable getPageable(int page, int size) {
        return PageRequest.of(Math.max(page - 1, 0), size,
                Sort.by(Sort.Direction.DESC, "createDate"));
    }

    protected Pageable getPageable(int page, int size, String sortField, Sort.Direction direction) {
        return PageRequest.of(Math.max(page - 1, 0), size, Sort.by(direction, sortField));
    }

    protected Pageable getPageable(SearchDto search) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(search.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(search.getPage() - 1, 0), search.getSize(),
                Sort.by(direction, search.getSortField()));
    }

    /** Page<T> → PageResponse<T> */
    protected <T> ResponseEntity<PageResponse<T>> toPageResponse(Page<T> page) {
        return ResponseEntity.ok(PageResponse.<T>builder()
                .result(true).message("success")
                .data(page.getContent())
                .pagePos(page.getNumber() + 1)
                .pageCnt(page.getTotalPages())
                .totalCnt(page.getTotalElements())
                .rowCnt(page.getSize())
                .build());
    }

    /** List<T> → PageResponse<T> */
    protected <T> ResponseEntity<PageResponse<T>> toPageResponse(
            List<T> data, int page, int totalPages, long totalCnt) {
        return ResponseEntity.ok(PageResponse.<T>builder()
                .result(true).message("success")
                .data(data).pagePos(page).pageCnt(totalPages)
                .totalCnt(totalCnt).rowCnt(ROW_CNT)
                .build());
    }

    // ───────── 공통 응답 ─────────

    protected <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> ok() {
        return ResponseEntity.ok(ApiResponse.success());
    }

    protected <T> ResponseEntity<ApiResponse<T>> fail(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }

    // ───────── 로깅 ─────────

    protected void logInfo(String message, Object... args) {
        log.info("[{}] " + message, getCurrentUserId(), args);
    }

    protected void logError(String message, Exception e) {
        log.error("[{}] {} | error: {}", getCurrentUserId(), message, e.getMessage());
    }
}