package com.team4.taskfarm.admin.domain.ops.dto;

import com.team4.taskfarm.common.entity.social.TbMail;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class MailHistoryResponse {
    private final String refKey;
    private final String title;
    private final long sentCount;
    private final String rewardSummary;
    private final String createDate;

    public MailHistoryResponse(MailHistoryRow r) {
        this.refKey = r.getRefKey();
        this.title = r.getTitle();
        this.sentCount = r.getSentCount();
        this.rewardSummary = summarize(r.getRewardType(), r.getRewardCoin(), r.getRewardTitle());
        this.createDate = r.getCreateDate() != null
                ? r.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "";
    }

    private static String summarize(TbMail.RewardType type, int coin, String title) {
        if (type == TbMail.RewardType.COIN) return "코인 " + coin;
        if (type == TbMail.RewardType.TITLE) return "칭호 " + title;
        return "보상 없음";
    }
}
