package com.team4.taskfarm.user.domain.mail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team4.taskfarm.common.entity.social.TbMail;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MailResponse {

    private Long id;
    private String title;
    private String body;
    private TbMail.RewardType rewardType;
    private int rewardCoin;
    private String rewardTitle;
    private String source;
    @JsonProperty("isClaimed")
    private boolean isClaimed;
    private LocalDateTime createDate;

    public static MailResponse of(TbMail mail) {
        return MailResponse.builder()
                .id(mail.getIdxMail())
                .title(mail.getTitle())
                .body(mail.getBody())
                .rewardType(mail.getRewardType())
                .rewardCoin(mail.getRewardCoin())
                .rewardTitle(mail.getRewardTitle())
                .source(mail.getSource())
                .isClaimed(mail.isClaimed())
                .createDate(mail.getCreateDate())
                .build();
    }
}
