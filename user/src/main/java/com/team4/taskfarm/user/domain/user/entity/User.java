package com.team4.taskfarm.user.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tbUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_User")
    private Long idxUser;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Pass", nullable = false, length = 255)
    private String pass;

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

    @Column(name = "CreateDate", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "UpdateDate", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    // ENUM 정의
    public enum Role { ROLE_USER, ROLE_ADMIN }
    public enum Status { ACTIVE, SUSPENDED }

    // 생성 메서드 (회원가입용)
    public static User create(String email, String encodedPass, String nickname) {
        User user = new User();
        user.email = email;
        user.pass = encodedPass;
        user.nickname = nickname;
        user.role = Role.ROLE_USER;
        user.status = Status.ACTIVE;
        user.exp = 0;
        user.level = 1;
        user.streak = 0;
        user.createDate = LocalDateTime.now();
        user.updateDate = LocalDateTime.now();
        return user;
    }

    // 프로필 수정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updateDate = LocalDateTime.now();
    }

    // 비밀번호 변경
    public void updatePass(String encodedPass) {
        this.pass = encodedPass;
        this.updateDate = LocalDateTime.now();
    }

    // 소프트 삭제 (탈퇴)
    public void withdraw() {
        this.deleteDate = LocalDateTime.now();
        this.status = Status.SUSPENDED;
    }
}