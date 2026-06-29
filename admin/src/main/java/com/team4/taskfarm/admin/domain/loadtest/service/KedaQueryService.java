package com.team4.taskfarm.admin.domain.loadtest.service;

import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

// KEDA의 ScaledObject는 기본 Kubernetes 타입이 아니라 Custom Resource라서
// GenericKubernetesResource로 조회한다.
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
// Kubernetes API를 Java 코드에서 호출하기 위한 Fabric8 Client
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KedaQueryService {

    // AI 추천 작업이 쌓이는 Redis Queue 이름.
    private static final String AI_RECOMMEND_QUEUE = "ai:recommend:queue";

    // KEDA와 taskfarm-user Deployment가 배포된 namespace.
    private static final String NAMESPACE = "taskfarm-prod";

    // KEDA가 스케일링 대상으로 바라보는 Deployment 이름.
    private static final String DEPLOYMENT_NAME = "taskfarm-user";

    // KEDA ScaledObject 이름.
    private static final String SCALED_OBJECT_NAME = "taskfarm-user-autoscaler";

    // Kubernetes API 조회에 실패했을 때 화면이 깨지지 않도록 사용할 기본값.
    private static final int DEFAULT_POD_COUNT = 1;

    // KEDA maxReplicaCount 조회 실패 시 사용할 기본값.
    private static final int DEFAULT_MAX_PODS = 5;

    // Redis 조회용 Template.
    private final StringRedisTemplate redisTemplate;

    // Kubernetes API 조회용 Client.
    private final KubernetesClient kubernetesClient;

    // Redis Queue 길이 조회.
    public int getQueueLength() {
        Long n = redisTemplate.opsForList().size(AI_RECOMMEND_QUEUE);

        // Redis에서 null이 올 가능성에 대비해서 null이면 0으로 처리한다.
        return n == null ? 0 : n.intValue();
    }

    // LoadTestService에서 queueLength라는 이름으로도 호출할 수 있게 만든 메서드.
    public int queueLength() {
        return getQueueLength();
    }

    // 현재 taskfarm-user Deployment의 Ready Pod 수 조회.
    public int getPodCount() {
        try {
            Integer readyReplicas = kubernetesClient.apps()
                    .deployments()
                    .inNamespace(NAMESPACE)
                    .withName(DEPLOYMENT_NAME)
                    .get()
                    .getStatus()
                    .getReadyReplicas();

            // readyReplicas가 null일 수 있다.
            return readyReplicas == null ? 0 : readyReplicas;

        } catch (Exception e) {
            // Kubernetes API 조회 실패 시 fallback.
            return DEFAULT_POD_COUNT;
        }
    }

    // LoadTestService에서 podCount라는 이름으로도 호출할 수 있게 만든 메서드.
    public int podCount() {
        return getPodCount();
    }

    // KEDA ScaledObject의 maxReplicaCount 조회.
    public int getMaxPods() {
        try {
            GenericKubernetesResource scaledObject = kubernetesClient.genericKubernetesResources(
                            "keda.sh/v1alpha1", // KEDA ScaledObject apiVersion
                            "ScaledObject"      // 조회할 Custom Resource kind
                    )
                    .inNamespace(NAMESPACE)
                    .withName(SCALED_OBJECT_NAME)
                    .get();

            // ScaledObject가 없거나 properties가 없으면 기본값 반환.
            if (scaledObject == null || scaledObject.getAdditionalProperties() == null) {
                return DEFAULT_MAX_PODS;
            }

            Object specObj = scaledObject.getAdditionalProperties().get("spec");

            if (!(specObj instanceof Map<?, ?> spec)) {
                return DEFAULT_MAX_PODS;
            }

            
            // spec.maxReplicaCount 값을 꺼낸다.
            Object maxReplicaCount = spec.get("maxReplicaCount");

            // 보통 숫자 타입으로 들어옴
            if (maxReplicaCount instanceof Number number) {
                return number.intValue();
            }

            // 문자열로 들어오는 경우 대비
            if (maxReplicaCount instanceof String str) {
                return Integer.parseInt(str);
            }

            return DEFAULT_MAX_PODS;

        } catch (Exception e) {
            // ScaledObject 조회 실패 시 fallback.
            return DEFAULT_MAX_PODS;
        }
    }

    // LoadTestService에서 maxPods라는 이름으로도 호출할 수 있게 만든 메서드.
    public int maxPods() {
        return getMaxPods();
    }
}