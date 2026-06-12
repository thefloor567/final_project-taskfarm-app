package com.team4.taskfarm.admin.domain.ops.dto;

import lombok.Getter;

@Getter
public class MailBroadcastResponse {
    private final int sentCount;

    public MailBroadcastResponse(int sentCount) {
        this.sentCount = sentCount;
    }
}
