package com.team4.taskfarm.common.entity.social;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 친구 관계 엔티티 (tbFriend).
 * - 작은ID-큰ID 1행 규칙: A-B를 두 행으로 저장하지 않는다.
 *   insert 시 항상 idxUserA = min(a,b), idxUserB = max(a,b).
 * - 교차신청(A→B 대기 중 B→A)은 새 행을 만들지 말고 기존 PENDING을 accept().
 * - FK는 연관관계 대신 Long 컬럼으로 직접 보유 (기존 컨벤션).
 */
@Entity
@Table(name = "tbFriend")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbFriend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Friend")
    private Long idxFriend;

    @Column(name = "Idx_User_A", nullable = false)
    private Long idxUserA;   // 항상 작은 UserId

    @Column(name = "Idx_User_B", nullable = false)
    private Long idxUserB;   // 항상 큰 UserId

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "RequestedBy", nullable = false)
    private Long requestedBy;   // 신청자 UserId

    @Column(name = "AcceptDate")
    private LocalDateTime acceptDate;

    public enum Status { PENDING, ACCEPTED }

    /**
     * 친구 신청 생성. 두 유저 ID를 정렬해 작은쪽=A, 큰쪽=B로 고정 저장.
     */
    public static TbFriend request(Long requesterId, Long targetId) {
        TbFriend f = new TbFriend();
        f.idxUserA = Math.min(requesterId, targetId);
        f.idxUserB = Math.max(requesterId, targetId);
        f.status = Status.PENDING;
        f.requestedBy = requesterId;
        return f;
    }

    /** 신청 수락 → ACCEPTED 전환 + 수락시각 기록. */
    public void accept() {
        this.status = Status.ACCEPTED;
        this.acceptDate = LocalDateTime.now();
    }

    /** 이 관계에서 상대방 ID 반환 (내 ID를 주면 상대 ID). */
    public Long opponentOf(Long myId) {
        return myId.equals(idxUserA) ? idxUserB : idxUserA;
    }

    public boolean isAccepted() {
        return this.status == Status.ACCEPTED;
    }
}