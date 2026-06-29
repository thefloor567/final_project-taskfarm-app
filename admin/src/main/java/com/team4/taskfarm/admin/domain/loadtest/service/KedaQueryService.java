package com.team4.taskfarm.admin.domain.loadtest.service;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KedaQueryService {

    // AI 추천 작업이 쌓이는 Redis Queue 이름.
    private static final String AI_RECOMMEND_QUEUE = "ai:recommend:queue";

    // Kubernetes API 조회가 실패했을 때 사용할 기본 Pod 수.
    private static final int DEFAULT_POD_COUNT = 1;

    // KEDA ScaledObject 조회가 실패했을 때 사용할 기본 maxPods 값.
    private static final int DEFAULT_MAX_PODS = 5;

    // RedisTemplate을 직접 필수 주입하지 않고 ObjectProvider로 받는다.
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    // Kubernetes API 조회용 Client도 ObjectProvider로 받는다.
    private final ObjectProvider<KubernetesClient> kubernetesClientProvider;

    // 조회할 namespace.
    @Value("${taskfarm.keda.namespace:taskfarm-dev}")
    private String namespace;

    // KEDA가 스케일링 대상으로 바라보는 Deployment 이름.
    @Value("${taskfarm.keda.deployment-name:taskfarm-user}")
    private String deploymentName;

    // KEDA ScaledObject 이름.
    @Value("${taskfarm.keda.scaled-object-name:taskfarm-user-autoscaler}")
    private String scaledObjectName;

    // Redis Queue 길이 조회
    public int getQueueLength() {
        try {
            StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();

            if (redisTemplate == null) {
                return 0;
            }

            Long n = redisTemplate.opsForList().size(AI_RECOMMEND_QUEUE);

            // Redis 조회 결과가 null일 수 있으므로 null이면 0 처리.
            return n == null ? 0 : n.intValue();

        } catch (Exception e) {
            // Redis 연결 실패, 인증 실패, 네트워크 문제 등이 있어도
            return 0;
        }
    }

    // LoadTestService에서 queueLength라는 이름으로도 호출할 수 있게 둔 메서드.
    public int queueLength() {
        return getQueueLength();
    }

    // 현재 taskfarm-user Deployment의 Ready Pod 수를 조회한다.
    public int getPodCount() {
        try {
            KubernetesClient kubernetesClient = kubernetesClientProvider.getIfAvailable();

            if (kubernetesClient == null) {
                return DEFAULT_POD_COUNT;
            }

            // 설정된 namespace에서 taskfarm-user Deployment 조회
            var deployment = kubernetesClient.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();

            // Deployment가 없거나 status가 아직 없으면 기본값 반환.
            if (deployment == null || deployment.getStatus() == null) {
                return DEFAULT_POD_COUNT;
            }

            // Ready 상태인 Pod 수를 꺼낸다.
            Integer readyReplicas = deployment.getStatus().getReadyReplicas();

            // readyReplicas가 null이면 아직 Ready Pod가 없거나 상태 정보가 갱신되지 않은 것이므로 0 반환.
            return readyReplicas == null ? 0 : readyReplicas;

        } catch (Exception e) {
            // Kubernetes API 조회 실패 시 기본값 반환.
            return DEFAULT_POD_COUNT;
        }
    }

    // LoadTestService에서 podCount라는 이름으로도 호출할 수 있게 둔 메서드.
    public int podCount() {
        return getPodCount();
    }

    // KEDA ScaledObject의 maxReplicaCount 조회
    public int getMaxPods() {
        try {
            KubernetesClient kubernetesClient = kubernetesClientProvider.getIfAvailable();

            if (kubernetesClient == null) {
                return DEFAULT_MAX_PODS;
            }

            // KEDA ScaledObject 조회.
            GenericKubernetesResource scaledObject = kubernetesClient.genericKubernetesResources(
                            "keda.sh/v1alpha1",
                            "ScaledObject"
                    )
                    .inNamespace(namespace)
                    .withName(scaledObjectName)
                    .get();

            // ScaledObject가 없거나 내부 속성이 없으면 기본값 반환.
            if (scaledObject == null || scaledObject.getAdditionalProperties() == null) {
                return DEFAULT_MAX_PODS;
            }

            Object specObj = scaledObject.getAdditionalProperties().get("spec");

            if (!(specObj instanceof Map<?, ?> spec)) {
                return DEFAULT_MAX_PODS;
            }

            Object maxReplicaCount = spec.get("maxReplicaCount");

            if (maxReplicaCount instanceof Number number) {
                return number.intValue();
            }

            if (maxReplicaCount instanceof String str) {
                return Integer.parseInt(str);
            }

            return DEFAULT_MAX_PODS;

        } catch (Exception e) {
            // ScaledObject 조회 실패 시 기본값 반환.
            return DEFAULT_MAX_PODS;
        }
    }

    // LoadTestService에서 maxPods라는 이름으로도 호출할 수 있게 둔 메서드.
    public int maxPods() {
        return getMaxPods();
    }
}