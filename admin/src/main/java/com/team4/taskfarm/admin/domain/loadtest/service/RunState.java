package com.team4.taskfarm.admin.domain.loadtest.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RunState {

    public enum Status {
        RUNNING,
        DONE,
        STOPPED,
        FAILED
    }

    private final String runId;
    private final String scenario;
    private final int total;
    private final int concurrency;
    private final AtomicInteger allocated = new AtomicInteger();
    private final AtomicInteger processed = new AtomicInteger();
    private final AtomicInteger success = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicReference<Status> status = new AtomicReference<>(Status.RUNNING);
    private final List<Long> elapsedMsList = Collections.synchronizedList(new ArrayList<>());

    private final AtomicReference<String> accessToken = new AtomicReference<>();

    public RunState(String runId, String scenario, int total, int concurrency) {
        this.runId = runId;
        this.scenario = scenario;
        this.total = total;
        this.concurrency = concurrency;
    }

    public String getRunId() {
        return runId;
    }

    public String getScenario() {
        return scenario;
    }

    public int getTotal() {
        return total;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getProcessed() {
        return processed.get();
    }

    public int getSuccess() {
        return success.get();
    }

    public int getFailed() {
        return failed.get();
    }

    public Status getStatus() {
        return status.get();
    }

    public boolean isRunning() {
        return status.get() == Status.RUNNING;
    }

    public void setAccessToken(String token) {
        accessToken.set(token);
    }

    public String getAccessToken() {
        return accessToken.get();
    }

    public void clearToken() {
        accessToken.set(null);
    }

    public boolean tryAllocate() {
        while (isRunning()) {
            int current = allocated.get();
            if (current >= total) {
                return false;
            }
            if (allocated.compareAndSet(current, current + 1)) {
                return true;
            }
        }
        return false;
    }

    public void recordSuccess(long elapsedMs) {
        success.incrementAndGet();
        processed.incrementAndGet();
        elapsedMsList.add(elapsedMs);
        finishIfComplete();
    }

    public void recordFailure() {
        failed.incrementAndGet();
        processed.incrementAndGet();
        finishIfComplete();
    }

    public void stop() {
        if (status.compareAndSet(Status.RUNNING, Status.STOPPED)) {
            clearToken();   // 중지 즉시 토큰 폐기
        }
    }

    public void fail() {
        if (status.compareAndSet(Status.RUNNING, Status.FAILED)) {
            clearToken();   // 실패 즉시 토큰 폐기
        }
    }

    public int getSuccessRate() {
        int currentProcessed = processed.get();
        if (currentProcessed == 0) {
            return 0;
        }
        return (int) Math.round((double) success.get() / currentProcessed * 100);
    }

    public long getAvgMs() {
        List<Long> snapshot = latencySnapshot();
        if (snapshot.isEmpty()) {
            return 0L;
        }
        long sum = 0L;
        for (long elapsedMs : snapshot) {
            sum += elapsedMs;
        }
        return Math.round((double) sum / snapshot.size());
    }

    public long getP95Ms() {
        List<Long> snapshot = latencySnapshot();
        if (snapshot.isEmpty()) {
            return 0L;
        }
        Collections.sort(snapshot);
        int index = (int) Math.ceil(snapshot.size() * 0.95) - 1;
        return snapshot.get(Math.max(index, 0));
    }

    private List<Long> latencySnapshot() {
        synchronized (elapsedMsList) {
            return new ArrayList<>(elapsedMsList);
        }
    }

    private void finishIfComplete() {
        if (processed.get() >= total) {
            if (status.compareAndSet(Status.RUNNING, Status.DONE)) {
                clearToken();   // 정상 완료 즉시 토큰 폐기
            }
        }
    }
}