package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbCrop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbCropRepository extends JpaRepository<TbCrop, Long> {

    /** 특정 밭에 현재 심긴 작물 (밭당 0~1개) */
    Optional<TbCrop> findByIdxPlot(Long idxPlot);

    /** 여러 밭의 작물을 한 번에 (밭 목록 렌더용 — N+1 방지) */
    List<TbCrop> findByIdxPlotIn(List<Long> idxPlots);
}