package com.team4.taskfarm.common.entity.social;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 우편함 엔티티 (tbMail).
 * - 리그/길드/이벤트/운영 보상의 공통 수령함.
 * - 보상은 코인·칭호만 (물방울 지급 불가 — 재화 원칙).
 * - 수령 멱등(CAS): claim()은 서비스에서 조건부 UPDATE로 처리
 *   (UPDATE ... SET IsClaimed=1 WHERE Idx_Mail=? AND IsClaimed=0).
 *   영향행=1일 때만 보상 지급. 엔티티의 claim()은 객체 상태 동기화용.
 * - 발송 멱등: (Idx_User, RefKey) UNIQUE 로 같은 보상 중복발송 차단.
 */
@Entity
@Table(name = "tbMail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbMail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Mail")
    private Long idxMail;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;   // 수신자

    @Column(name = "Title", nullable = false, length = 50)
    private String title;

    @Column(name = "Body", length = 200)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "RewardType", nullable = false)
    private RewardType rewardType = RewardType.NONE;

    @Column(name = "RewardCoin", nullable = false)
    private int rewardCoin = 0;   // 물방울 불가

    @Column(name = "RewardTitle", length = 50)
    private String rewardTitle;

    @Column(name = "IsClaimed", nullable = false)
    private boolean isClaimed = false;

    @Column(name = "Source", nullable = false, length = 20)
    private String source;   // LEAGUE | GUILD | EVENT | SYSTEM

    @Column(name = "RefKey", nullable = false, length = 100)
    private String refKey;   // 중복발송 방지키

    @Column(name = "ExpireAt")
    private LocalDateTime expireAt;

    @Column(name = "ClaimDate")
    private LocalDateTime claimDate;

    public enum RewardType { COIN, TITLE, NONE }

    /** 코인 보상 우편 발송. */
    public static TbMail ofCoin(Long idxUser, String title, String body,
                                int coin, String source, String refKey) {
        TbMail m = baseOf(idxUser, title, body, source, refKey);
        m.rewardType = RewardType.COIN;
        m.rewardCoin = coin;
        return m;
    }

    /** 칭호 보상 우편 발송. */
    public static TbMail ofTitle(Long idxUser, String title, String body,
                                 String rewardTitle, String source, String refKey) {
        TbMail m = baseOf(idxUser, title, body, source, refKey);
        m.rewardType = RewardType.TITLE;
        m.rewardTitle = rewardTitle;
        return m;
    }

    /** 보상 없는 공지 우편. */
    public static TbMail ofNotice(Long idxUser, String title, String body,
                                  String source, String refKey) {
        TbMail m = baseOf(idxUser, title, body, source, refKey);
        m.rewardType = RewardType.NONE;
        return m;
    }

    private static TbMail baseOf(Long idxUser, String title, String body,
                                 String source, String refKey) {
        TbMail m = new TbMail();
        m.idxUser = idxUser;
        m.title = title;
        m.body = body;
        m.source = source;
        m.refKey = refKey;
        m.isClaimed = false;
        return m;
    }

    /**
     * 수령 처리 (객체 상태 동기화).
     * ⚠️ 실제 멱등 보장은 서비스의 조건부 UPDATE(CAS)가 담당.
     * 만료/이미수령이면 false 반환.
     */
    public boolean claim() {
        if (this.isClaimed) return false;
        if (this.expireAt != null && this.expireAt.isBefore(LocalDateTime.now())) return false;
        this.isClaimed = true;
        this.claimDate = LocalDateTime.now();
        return true;
    }

    public boolean isExpired() {
        return expireAt != null && expireAt.isBefore(LocalDateTime.now());
    }
}