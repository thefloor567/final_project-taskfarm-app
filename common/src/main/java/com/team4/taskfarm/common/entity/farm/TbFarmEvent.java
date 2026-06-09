package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 발생 이벤트 (tbFarmEvent). 일별, 리롤 방지.
 * (유저ID+날짜) 시드 고정 생성. UNIQUE(Idx_User, EventDate)로 같은 날 리롤 차단.
 */
@Entity
@Table(name = "tbFarmEvent",
       uniqueConstraints = @UniqueConstraint(name = "uk_FarmEvent_UserDate",
                                             columnNames = {"Idx_User", "EventDate"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbFarmEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_FarmEvent")
    private Long idxFarmEvent;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

    @Column(name = "EventKey", nullable = false, length = 30)
    private String eventKey;

    @Column(name = "EventDate", nullable = false)
    private LocalDate eventDate;

    @Column(name = "IsDismissed", nullable = false)
    private boolean isDismissed = false;

    @CreatedDate
    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createDate;

    // ✏️ TODO: dismiss()
    /** (유저+날짜) 이벤트 발생 기록 생성. 시드로 뽑은 eventKey 저장. */
    public static TbFarmEvent create(Long idxUser, String eventKey, java.time.LocalDate date) {
        TbFarmEvent e = new TbFarmEvent();
        e.idxUser = idxUser;
        e.eventKey = eventKey;
        e.eventDate = date;
        e.isDismissed = false;
        return e;
    }
    
    /** 당일 이벤트 무효화(온실 보호 등). IsDismissed = true */
    public void dismiss() {
        this.isDismissed = true;
    }
}