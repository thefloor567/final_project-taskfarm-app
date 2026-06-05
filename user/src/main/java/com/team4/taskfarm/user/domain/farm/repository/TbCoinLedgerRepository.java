package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbCoinLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbCoinLedgerRepository extends JpaRepository<TbCoinLedger, Long> {

    /** 농장 코인 거래내역 (최신순) — 추후 내역 화면/검증용 */
    List<TbCoinLedger> findByIdxFarmOrderByIdxCoinDesc(Long idxFarm);
}