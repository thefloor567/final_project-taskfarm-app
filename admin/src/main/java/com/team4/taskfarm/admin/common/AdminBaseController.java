package com.team4.taskfarm.admin.common;

import com.team4.taskfarm.common.controller.BaseController;

/**
 * admin 앱 전용 공통 컨트롤러 베이스.
 * common.BaseController 상속. 어드민의 핵심은 ROLE_ADMIN 체크(isAdmin()은 베이스 제공).
 * admin의 모든 컨트롤러는 이 클래스를 상속.
 *
 * ⚠️ 어드민 페이지 진입 시 권한 체크 패턴:
 *    if (!isAdmin()) return "redirect:/";   // 뷰 컨트롤러
 *    (API는 SecurityConfig에서 hasRole("ADMIN")로 막는 게 더 깔끔)
 */
public abstract class AdminBaseController extends BaseController {

    // 어드민 전용 공통 기능이 생기면 여기에 추가
    // 예) 어드민 활동 로그 기록 등
    //
    // 필요하면 user처럼 getCurrentAdmin()도 가능하지만,
    // 어드민은 보통 통계·관리 위주라 엔티티 직접 조회가 적음.
}