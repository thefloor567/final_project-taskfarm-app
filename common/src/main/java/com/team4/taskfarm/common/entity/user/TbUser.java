package com.team4.taskfarm.common.entity.user;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 유저 엔티티 (tbUser).
 * 자바 필드 camelCase, 컬럼명은 @Column(name=...)로 명세서에 매핑.
 * CreateDate/UpdateDate는 BaseEntity, DeleteDate(소프트삭제)는 직접.
 */
@Entity
@Table(name = "tbUser")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_User")
    private Long idxUser;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Pass", nullable = false, length = 255)
    private String pass;  // BCrypt 해시

    @Column(name = "Nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "Exp", nullable = false)
    private int exp = 0;

    @Column(name = "Level", nullable = false)
    private int level = 1;

    @Column(name = "Streak", nullable = false)
    private int streak = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    public enum Role { ROLE_USER, ROLE_ADMIN }
    public enum Status { ACTIVE, SUSPENDED }

    // ✏️ TODO: 빌더/정적팩토리, 상태변경 메서드(updateNickname, addExp, levelUp 등)
    // ───────── 상수 (레벨디자인 ③) ─────────
    private static final int MAX_LEVEL = 10;

    // ───────── 생성 (회원가입) ─────────
    public static TbUser create(String email, String encodedPass, String nickname) {
        TbUser user = new TbUser();
        user.email = email;
        user.pass = encodedPass;
        user.nickname = nickname;
        user.role = Role.ROLE_USER;
        user.status = Status.ACTIVE;
        user.exp = 0;
        user.level = 1;
        user.streak = 0;
        return user;
    }

    // ───────── 프로필/계정 ─────────
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePass(String encodedPass) {
        this.pass = encodedPass;
    }

    public void withdraw() {
        this.deleteDate = java.time.LocalDateTime.now();
        this.status = Status.SUSPENDED;
    }

    // ───────── 경험치/레벨 (레벨디자인 ①②) ─────────

    /** cumExp(n) = 25 * n * (n-1) — n레벨 도달에 필요한 누적 Exp */
    private static int cumExp(int n) {
        return 25 * n * (n - 1);
    }

    /** 누적 Exp로 현재 레벨 역산 */
    private static int levelFromExp(int exp) {
        int lv = 1;
        while (lv < MAX_LEVEL && exp >= cumExp(lv + 1)) {
            lv++;
        }
        return lv;
    }

    /**
     * todo 완료 시 XP 적립 + 레벨업 판정. Exp는 누적이라 절대 안 깎임.
     * @return 이번에 오른 레벨 수 (0이면 레벨업 없음). 물방울 보너스 계산에 사용.
     */
    public int earnExp(int granted) {
        int oldLevel = this.level;
        this.exp += granted;
        int newLevel = levelFromExp(this.exp);
        int levelsGained = newLevel - oldLevel;
        if (levelsGained > 0) {
            this.level = newLevel;
        }
        return levelsGained;
    }
}