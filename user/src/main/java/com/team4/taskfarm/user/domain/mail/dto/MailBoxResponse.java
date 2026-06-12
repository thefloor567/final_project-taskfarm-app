package com.team4.taskfarm.user.domain.mail.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MailBoxResponse {

    private long unread;
    private List<MailResponse> mails;

    public static MailBoxResponse of(long unread, List<MailResponse> mails) {
        return MailBoxResponse.builder()
                .unread(unread)
                .mails(mails)
                .build();
    }
}
