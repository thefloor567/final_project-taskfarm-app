package com.team4.taskfarm.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티가 상속하는 공통 베이스.
 * - 생성일시/수정일시를 JPA Auditing으로 자동 기록.
 * - @MappedSuperclass: 이 클래스 자체는 테이블이 아니고,
 *   상속한 엔티티의 컬럼으로 합쳐짐.
 *
 * ⚠️ Auditing이 동작하려면 각 앱의 메인 클래스(또는 설정 클래스)에
 *    @EnableJpaAuditing 을 반드시 붙여야 함. (안 붙이면 createDate가 null)
 *
 * 컬럼명을 createDate 로 둔 이유: BaseController의 페이지네이션 기본 정렬이
 * "createDate" 기준이라 일치시킴.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(name = "UpdateDate")
    private LocalDateTime updateDate;
}
