package com.team4.taskfarm.user.common;

import com.team4.taskfarm.common.controller.BaseController;

/**
 * user 앱 전용 공통 컨트롤러 베이스.
 * common.BaseController(앱 무관 공통) 상속 + user 전용 공통(getCurrentUser 등) 추가.
 * user의 모든 컨트롤러(뷰·API)는 이 클래스를 상속.
 *
 * ⚠️ getCurrentUser()는 TbUserRepository가 있어야 동작.
 *    auth 도메인에서 TbUserRepository 만든 뒤 아래 주석을 풀 것.
 */
public abstract class UserBaseController extends BaseController {

    // ── TbUserRepository 생기면 아래 주석 해제 ──
    //
    // @org.springframework.beans.factory.annotation.Autowired
    // protected com.team4.taskfarm.user.domain.user.repository.TbUserRepository userRepository;
    //
    // /** 현재 로그인 유저 엔티티 조회 (subject = email) */
    // protected com.team4.taskfarm.common.entity.user.TbUser getCurrentUser() {
    //     String email = getCurrentUserId();
    //     if (email == null) return null;
    //     return userRepository.findByEmail(email).orElse(null);
    // }

    // 예: 유저 캐시 조회 등 user 전용 공통이 더 생기면 여기에 추가
}