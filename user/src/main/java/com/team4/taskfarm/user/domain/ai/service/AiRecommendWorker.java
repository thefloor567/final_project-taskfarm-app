package com.team4.taskfarm.user.domain.ai.service;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.ai.TbAiLog;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobRequest;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobResult;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendResult;
import com.team4.taskfarm.user.domain.ai.repository.AiLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("worker")
@RequiredArgsConstructor
public class AiRecommendWorker {

    // Redis Queue에서 작업을 꺼내고, job 결과를 저장하는 서비스
    private final AiRecommendQueueService queueService;

    // 실제 경험치 추천 처리 담당 = 내부에서 Redis 캐시 확인 → 없으면 Gemini 호출 → clamp 처리까지 한다.
    private final GeminiExpRecommendService geminiExpRecommendService;

    // tbAiLog에 추천 결과를 저장하기 위한 Repository
    private final AiLogRepository aiLogRepository;

    // 1초마다 Redis Queue를 확인 = 큐에 작업이 있으면 processJob()으로 처리, 작업이 없으면 아무것도 하지 않음
    @Scheduled(fixedDelay = 1000) // 이전 작업 실행이 끝난 뒤 1초 후 다시 실행
    @Transactional
    public void processOneJob() {
        queueService.popJob()
                .ifPresent(this::processJob);
    }

    
    // Redis Queue에서 꺼낸 작업 하나 처리 = Redis 캐시 확인 또는 Gemini 호출 => 추천 결과를 tbAiLog에 저장 
    // => 프론트가 조회할 수 있도록 job 결과를 DONE으로 Redis에 저장 => 실패하면 job 결과를 FAILED로 저장
    private void processJob(AiRecommendJobRequest job) {
        try {
            log.info("AI 추천 작업 처리 시작 - jobId={}, idxTodo={}",
                    job.jobId(), job.idxTodo());

            AiRecommendResult result = geminiExpRecommendService.recommendExp(
                    job.categoryName(),
                    job.priority(),
                    job.title()
            );

            aiLogRepository.save(
                    TbAiLog.create(
                            job.idxUser(),
                            job.idxTodo(),
                            result.rewardExp(),
                            result.isCache(),
                            result.token()
                    )
            );

            // 프론트 폴링용 결과 저장 = 사용자는 처음 요청할 때 jobId만 받음. 이후 프론트가 GET /jobs/{jobId}로 계속 조회
            queueService.saveJobResult(
                    AiRecommendJobResult.done(
                            job.jobId(),
                            result.rewardExp(),
                            result.isCache()
                    )
            );

            log.info("AI 추천 작업 처리 완료 - jobId={}, rewardExp={}, isCache={}",
                    job.jobId(), result.rewardExp(), result.isCache());

        } catch (Exception e) {

            log.error("AI 추천 작업 처리 실패 - jobId={}", job.jobId(), e);

            queueService.saveJobResult(
                    AiRecommendJobResult.failed(
                            job.jobId(),
                            "AI 경험치 추천 처리에 실패했습니다."
                    )
            );
        }
    }
}