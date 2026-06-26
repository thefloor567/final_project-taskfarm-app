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

    // 소셜: 친구코드 (가입 시 랜덤·고유). 기존 유저는 마이그레이션으로 백필.
    @Column(name = "FriendCode", length = 12)
    private String friendCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "Exp", nullable = false)
    private int exp = 0;

    @Column(name = "Level", nullable = false)
    private int level = 1;

    @Column(name = "Streak", nullable = false)
    private int streak = 0;

    // 업적: 장착 대표칭호 (랭킹·친구목록에 노출되는 자랑용). null=미장착.
    @Column(name = "EquippedTitle", length = 50)
    private String equippedTitle;

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
        // ※ friendCode는 여기서 안 채움 — AuthService가 중복 없는 코드를
        //   생성해 assignFriendCode()로 주입 (UNIQUE 충돌 시 재시도 책임은 서비스).
        return user;
    }

    // ───────── 프로필/계정 ─────────
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // ───────── 소셜/업적 ─────────
    /** 친구코드 부여 (가입 시 1회. 이미 있으면 변경하지 않음). */
    public void assignFriendCode(String code) {
        if (this.friendCode == null) {
            this.friendCode = code;
        }
    }

    /** 대표 칭호 장착. 보유(달성) 칭호인지는 서비스에서 검증 후 호출. */
    public void equipTitle(String title) {
        this.equippedTitle = title;
    }

    public void updatePass(String encodedPass) {
        this.pass = encodedPass;
    }

    public void withdraw() {
        this.deleteDate = java.time.LocalDateTime.now();
        this.status = Status.SUSPENDED;
    }

    public void suspend() {
        this.status = Status.SUSPENDED;
    }

    public void activate() {
        this.status = Status.ACTIVE;
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
    
    
    public int expInCurrentLevel() {
    	return this.exp - cumExp(this.level);
    }
    
    public int expNeededForLevel() {
    	if (this.level >= MAX_LEVEL) return 0;
    	return cumExp(this.level +1) - cumExp(this.level);
    }
    
    
    
}