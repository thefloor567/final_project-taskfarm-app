package com.team4.taskfarm.common.entity.todo;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 할일 엔티티 (tbTodo).
 * 카테고리(단일/선택)·우선순위(A/B/C)·마감일. 완료 시 XP/물방울 지급.
 */
@Entity
@Table(name = "tbTodo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbTodo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Todo")
    private Long idxTodo;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

    @Column(name = "Idx_Cat")
    private Long idxCat;    // NULL 허용

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "Priority", nullable = false)
    private Priority priority = Priority.C;

    @Column(name = "IsDone", nullable = false)
    private boolean isDone = false;

    @Column(name = "DueDate")
    private LocalDateTime dueDate;

    @Column(name = "CompleteDate")
    private LocalDateTime completeDate;

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    public enum Priority { A, B, C }

    // ✏️ TODO: complete(), update 메서드
}