package com.team4.taskfarm.user.domain.category.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tbCategory")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Cat")
    private Long idxCat;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Color", nullable = false, length = 20)
    private String color;

    @Column(name = "CreateDate", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "UpdateDate", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    // 생성 메서드
    public static Category create(Long idxUser, String name, String color) {
        Category category = new Category();
        category.idxUser = idxUser;
        category.name = name;
        category.color = color;
        category.createDate = LocalDateTime.now();
        category.updateDate = LocalDateTime.now();
        return category;
    }

    // 수정 메서드
    public void update(String name, String color) {
        if (name != null) this.name = name;
        if (color != null) this.color = color;
        this.updateDate = LocalDateTime.now();
    }

    // 소프트 삭제
    public void delete() {
        this.deleteDate = LocalDateTime.now();
    }
}