package com.team4.taskfarm.admin.domain.loadtest.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.team4.taskfarm.admin.domain.loadtest.dto.LoadTestRequest;
import com.team4.taskfarm.admin.domain.loadtest.dto.RunStatusResponse;
import com.team4.taskfarm.admin.domain.loadtest.scenario.LoadScenario;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoadTestService {

    private final Map<String, RunState> runs = new ConcurrentHashMap<>();
    private final Map<String, LoadScenario> scenarios = new ConcurrentHashMap<>();
    private final ObjectProvider<KedaQueryService> kedaQueryServiceProvider;
    private final ExecutorService workerPool = Executors.newCachedThreadPool();

    public LoadTestService(
            Map<String, LoadScenario> scenarioBeans,
            ObjectProvider<KedaQueryService> kedaQueryServiceProvider) {
        this.kedaQueryServiceProvider = kedaQueryServiceProvider;
        scenarioBeans.forEach(this::registerScenario);
    }

    public String start(LoadTestRequest request) {
        if (hasRunningRun()) {
            throw new IllegalArgumentException("이미 실행 중");
        }

        String scenarioName = normalize(request.getScenario());
        LoadScenario scenario = Optional.ofNullable(scenarios.get(scenarioName))
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 시나리오입니다: " + request.getScenario()));

        String runId = UUID.randomUUID().toString().substring(0, 8);
        RunState state = new RunState(runId, scenarioName, request.getTotalRequests(), request.getConcurrency());
        runs.put(runId, state);

        for (int i = 0; i < request.getConcurrency(); i++) {
            workerPool.submit(() -> runWorker(state, scenario));
        }

        return runId;
    }

    public RunStatusResponse getStatus(String runId) {
        RunState state = getRunState(runId);
        return RunStatusResponse.builder()
                .status(state.getStatus().name())
                .total(state.getTotal())
                .processed(state.getProcessed())
                .success(state.getSuccess())
                .failed(state.getFailed())
                .successRate(state.getSuccessRate())
                .avgMs(state.getAvgMs())
                .p95Ms(state.getP95Ms())
                .queueLength(queryKedaMetric("getQueueLength", "queueLength"))
                .podCount(queryKedaMetric("getPodCount", "podCount"))
                .maxPods(queryKedaMetric("getMaxPods", "maxPods"))
                .build();
    }

    public void stop(String runId) {
        getRunState(runId).stop();
    }

    @PreDestroy
    public void shutdown() {
        workerPool.shutdownNow();
    }

    private void runWorker(RunState state, LoadScenario scenario) {
        while (state.tryAllocate()) {
            long startedAt = System.nanoTime();
            try {
                scenario.runOnce();
                long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
                state.recordSuccess(elapsedMs);
            } catch (Exception e) {
                log.warn("loadtest scenario failed. runId={}, scenario={}", state.getRunId(), state.getScenario(), e);
                state.recordFailure();
            }
        }
    }

    private RunState getRunState(String runId) {
        return Optional.ofNullable(runs.get(runId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 runId입니다: " + runId));
    }

    private boolean hasRunningRun() {
        return runs.values().stream().anyMatch(RunState::isRunning);
    }

    private void registerScenario(String beanName, LoadScenario scenario) {
        for (String key : toScenarioKeys(beanName, scenario)) {
            scenarios.put(key, scenario);
        }
    }

    private List<String> toScenarioKeys(String beanName, LoadScenario scenario) {
        List<String> keys = new ArrayList<>();
        String simpleName = scenario.getClass().getSimpleName();
        if (simpleName.endsWith("Scenario")) {
            keys.add(normalize(simpleName.substring(0, simpleName.length() - "Scenario".length())));
        }

        String lowerBeanName = beanName.toLowerCase(Locale.ROOT);
        if (lowerBeanName.endsWith("scenario")) {
            keys.add(normalize(beanName.substring(0, beanName.length() - "scenario".length())));
        }
        keys.add(normalize(beanName));
        return keys;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim()
                .replace("-", "_")
                .replaceAll("([a-z])([A-Z])", "$1_$2");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private int queryKedaMetric(String... candidateMethods) {
        KedaQueryService kedaQueryService = kedaQueryServiceProvider.getIfAvailable();
        if (kedaQueryService == null) {
            return 0;
        }

        for (String methodName : candidateMethods) {
            try {
                Method method = kedaQueryService.getClass().getMethod(methodName);
                Object result = method.invoke(kedaQueryService);
                if (result instanceof Number number) {
                    return number.intValue();
                }
            } catch (ReflectiveOperationException ignored) {
                // Team 3 will own KedaQueryService; keep engine compilable until its API is finalized.
            }
        }
        return 0;
    }
}
