package com.team4.taskfarm.user.domain.mail.service;

import com.team4.taskfarm.common.entity.social.TbMail;
import com.team4.taskfarm.user.domain.mail.repository.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendService {

    private final MailRepository mailRepository;
    
    @Transactional
    public boolean sendCoinReward(Long idxUser, int coin, String title, String body,
                                  String source, String refKey) {
        if (idxUser == null || coin <= 0) {
            return false;
        }

        // 멱등: 같은 보상 중복 발송 차단
        if (mailRepository.existsByRefKeyAndIdxUser(refKey, idxUser)) {
            log.info("보상 우편 이미 발송됨 - idxUser={}, refKey={}", idxUser, refKey);
            return false;
        }

        try {
            mailRepository.save(
                    TbMail.ofCoin(idxUser, title, body, coin, source, refKey));
            log.info("보상 우편 발송 - idxUser={}, coin={}, refKey={}", idxUser, coin, refKey);
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // (refKey, idxUser) UNIQUE 동시성 충돌 → 이미 발송됨. 멱등 보장.
            log.debug("보상 우편 중복 발송 무시 - idxUser={}, refKey={}", idxUser, refKey);
            return false;
        }
    }

    /** 보상 없는 공지 우편 (선택). */
    @Transactional
    public boolean sendNotice(Long idxUser, String title, String body,
                              String source, String refKey) {
        if (idxUser == null) return false;
        if (mailRepository.existsByRefKeyAndIdxUser(refKey, idxUser)) return false;
        try {
            mailRepository.save(TbMail.ofNotice(idxUser, title, body, source, refKey));
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return false;
        }
    }
}