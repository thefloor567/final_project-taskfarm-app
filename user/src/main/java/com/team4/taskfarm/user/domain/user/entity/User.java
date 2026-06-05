package com.team4.taskfarm.user.domain.user.entity;

import java.time.LocalDateTime;

import com.team4.taskfarm.common.entity.BaseEntity;

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
public class User extends BaseEntity {

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

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    public enum Role {
        ROLE_USER, ROLE_ADMIN
    }

    public enum Status {
        ACTIVE, SUSPENDED
    }

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
        return user;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePass(String encodedPass) {
        this.pass = encodedPass;
    }

    public void withdraw() {
        this.deleteDate = LocalDateTime.now();
        this.status = Status.SUSPENDED;
    }
}