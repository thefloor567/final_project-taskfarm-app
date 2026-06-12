package com.team4.taskfarm.user.domain.mail.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MailClaimResponse {

    private int claimedCount;
    private int earnedCoin;

    public static MailClaimResponse of(int claimedCount, int earnedCoin) {
        return MailClaimResponse.builder()
                .claimedCount(claimedCount)
                .earnedCoin(earnedCoin)
                .build();
    }
}
