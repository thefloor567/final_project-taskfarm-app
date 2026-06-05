package com.team4.taskfarm.user.domain.category.entity;

import java.time.LocalDateTime;

import com.team4.taskfarm.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tbCategory")
public class Category extends BaseEntity {

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

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    public static Category create(Long idxUser, String name, String color) {
        Category category = new Category();
        category.idxUser = idxUser;
        category.name = name;
        category.color = color;
        return category;
    }

    public void update(String name, String color) {
        if (name != null) {
            this.name = name;
        }

        if (color != null) {
            this.color = color;
        }
    }

    public void delete() {
        this.deleteDate = LocalDateTime.now();
    }
}