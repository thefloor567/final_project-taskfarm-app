package com.team4.taskfarm.admin.domain.loadtest.scenario;

public interface LoadScenario {

    void runOnce() throws Exception;

    /**
     * 실행 단위로 전달되는 컨텍스트(액세스 토큰 등)를 받는 실행 메서드.
     *
     * 기본 구현은 토큰을 무시하고 {@link #runOnce()}를 호출한다.
     * 따라서 토큰이 필요 없는 기존 시나리오들은 이 메서드를 구현할 필요가 없고,
     * 시그니처 변경의 영향도 받지 않는다.
     *
     * 토큰이 필요한 시나리오(AUTHED_API)만 이 메서드를 오버라이드한다.
     *
     * @param accessToken 이번 실행에만 사용되는 휘발성 JWT (없으면 null/blank)
     */
    default void runOnce(String accessToken) throws Exception {
        runOnce();
    }
}