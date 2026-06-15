package com.team4.taskfarm.admin.domain.ops.service;

import com.team4.taskfarm.admin.domain.ops.dto.MailBroadcastRequest;
import com.team4.taskfarm.admin.domain.ops.dto.MailBroadcastResponse;
import com.team4.taskfarm.admin.domain.ops.dto.MailHistoryResponse;
import com.team4.taskfarm.admin.domain.ops.repository.AdminMailRepository;
import com.team4.taskfarm.common.entity.social.TbMail;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MailOpsService {

    private static final String SOURCE_ADMIN = "SYSTEM";

    private final AdminMailRepository mailRepository;

    @Transactional
    public MailBroadcastResponse broadcast(MailBroadcastRequest req) {
        // RefKey 자동 생성 (빈칸이면)
        String refKey = (req.getRefKey() == null || req.getRefKey().isBlank())
                ? "auto-" + System.currentTimeMillis()
                : req.getRefKey().trim();

        // 발송 대상 결정
        List<Long> targets;
        if ("ALL".equals(req.getTargetType())) {
            targets = mailRepository.findAllActiveUserIds();
        } else {
            targets = req.getTargetUserIds();
            if (targets == null || targets.isEmpty()) {
                throw CustomException.badRequest("발송 대상 유저를 지정해주세요.");
            }
        }

        // 멱등: 같은 RefKey로 이미 받은 유저 제외
        Set<Long> already = new HashSet<>(mailRepository.findUserIdsByRefKey(refKey));

        String title = req.getTitle();
        String body = req.getBody();
        List<TbMail> mails = new ArrayList<>();
        for (Long userId : targets) {
            if (already.contains(userId)) continue;
            mails.add(buildMail(req, userId, title, body, refKey));
        }

        mailRepository.saveAll(mails);
        return new MailBroadcastResponse(mails.size());
    }

    private TbMail buildMail(MailBroadcastRequest req, Long userId, String title, String body, String refKey) {
        // 보상은 코인
        return switch (req.getRewardType() == null ? "NONE" : req.getRewardType()) {
            case "COIN" -> TbMail.ofCoin(userId, title, body, req.getRewardCoin(), SOURCE_ADMIN, refKey);
            default -> TbMail.ofNotice(userId, title, body, SOURCE_ADMIN, refKey);
        };
    }

    @Transactional(readOnly = true)
    public List<MailHistoryResponse> getHistory(int limit) {
        return mailRepository.findHistory(PageRequest.of(0, limit)).stream()
                .map(MailHistoryResponse::new)
                .collect(Collectors.toList());
    }
}
