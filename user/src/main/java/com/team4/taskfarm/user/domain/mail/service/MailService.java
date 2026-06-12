package com.team4.taskfarm.user.domain.mail.service;

import com.team4.taskfarm.common.entity.farm.TbCoinLedger;
import com.team4.taskfarm.common.entity.farm.TbFarm;
import com.team4.taskfarm.common.entity.social.TbMail;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.farm.repository.TbCoinLedgerRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;
import com.team4.taskfarm.user.domain.mail.dto.MailBoxResponse;
import com.team4.taskfarm.user.domain.mail.dto.MailClaimResponse;
import com.team4.taskfarm.user.domain.mail.dto.MailResponse;
import com.team4.taskfarm.user.domain.mail.repository.MailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final String MAIL_CLAIM_REASON = "MAIL_CLAIM";

    private final MailRepository mailRepository;
    private final TbFarmRepository farmRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;

    @Transactional(readOnly = true)
    public MailBoxResponse getMailBox(Long idxUser) {
        Long userId = requireUser(idxUser);
        LocalDateTime now = LocalDateTime.now();
        List<MailResponse> mails = mailRepository.findByIdxUserOrderByCreateDateDesc(userId)
                .stream()
                .map(MailResponse::of)
                .toList();

        return MailBoxResponse.of(mailRepository.countUnread(userId, now), mails);
    }

    @Transactional
    public MailClaimResponse claimMail(Long idxUser, Long mailId) {
        Long userId = requireUser(idxUser);
        TbMail mail = getOwnedMail(mailId, userId);
        int earnedCoin = claimOne(userId, mail, LocalDateTime.now());
        return MailClaimResponse.of(1, earnedCoin);
    }

    @Transactional
    public MailClaimResponse claimAll(Long idxUser) {
        Long userId = requireUser(idxUser);
        LocalDateTime now = LocalDateTime.now();
        List<TbMail> claimableMails = mailRepository.findClaimableMails(userId, now);
        TbFarm farm = hasCoinReward(claimableMails) ? getFarm(userId) : null;
        int claimedCount = 0;
        int earnedCoin = 0;

        for (TbMail mail : claimableMails) {
            if (mail.getRewardType() == TbMail.RewardType.TITLE) {
                continue;
            }
            int coin;
            try {
                coin = claimOne(userId, mail, now, farm);
            } catch (CustomException e) {
                continue;
            }
            claimedCount++;
            earnedCoin += coin;
        }

        return MailClaimResponse.of(claimedCount, earnedCoin);
    }

    private int claimOne(Long idxUser, TbMail mail, LocalDateTime now) {
        return claimOne(idxUser, mail, now, null);
    }

    private int claimOne(Long idxUser, TbMail mail, LocalDateTime now, TbFarm farm) {
        if (mail.getRewardType() == TbMail.RewardType.TITLE) {
            throw CustomException.badRequest("칭호 보상 우편은 아직 수령할 수 없습니다.");
        }

        int updated = mailRepository.claimIfUnclaimed(mail.getIdxMail(), idxUser, now);
        if (updated != 1) {
            throw CustomException.badRequest("이미 수령했거나 만료된 우편입니다.");
        }

        if (mail.getRewardType() == TbMail.RewardType.COIN && mail.getRewardCoin() > 0) {
            TbFarm targetFarm = farm != null ? farm : getFarm(idxUser);
            targetFarm.earnCoin(mail.getRewardCoin());
            coinLedgerRepository.save(TbCoinLedger.earn(
                    targetFarm.getIdxFarm(),
                    mail.getRewardCoin(),
                    MAIL_CLAIM_REASON,
                    mail.getIdxMail()
            ));
            return mail.getRewardCoin();
        }

        return 0;
    }

    private boolean hasCoinReward(List<TbMail> mails) {
        return mails.stream()
                .anyMatch(mail -> mail.getRewardType() == TbMail.RewardType.COIN
                        && mail.getRewardCoin() > 0);
    }

    private TbMail getOwnedMail(Long mailId, Long idxUser) {
        return mailRepository.findByIdxMailAndIdxUser(mailId, idxUser)
                .orElseThrow(() -> CustomException.notFound("우편을 찾을 수 없습니다."));
    }

    private TbFarm getFarm(Long idxUser) {
        return farmRepository.findByIdxUser(idxUser)
                .orElseThrow(() -> CustomException.notFound("농장을 찾을 수 없습니다."));
    }

    private Long requireUser(Long idxUser) {
        if (idxUser == null) {
            throw CustomException.unauthorized("로그인이 필요합니다.");
        }
        return idxUser;
    }
}
