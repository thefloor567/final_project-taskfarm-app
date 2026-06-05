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
}