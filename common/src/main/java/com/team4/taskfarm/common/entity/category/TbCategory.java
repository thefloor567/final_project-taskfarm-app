package com.team4.taskfarm.common.entity.category;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카테고리 엔티티 (tbCategory).
 * 유저별 이름+색. 할일에 단일 연결. 삭제 시 연결 할일은 NULL 처리.
 */
@Entity
@Table(name = "tbCategory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Cat")
    private Long idxCat;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;   // FK → tbUser
    // ✏️ 연관관계로 바꾸려면: @ManyToOne(fetch=LAZY) @JoinColumn(name="Idx_User") private TbUser user;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Color", nullable = false, length = 20)
    private String color = "#cccccc";

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    // ✏️ TODO: 빌더, updateName/updateColor
}