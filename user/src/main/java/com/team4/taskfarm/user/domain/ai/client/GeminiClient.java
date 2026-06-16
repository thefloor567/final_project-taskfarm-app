package com.team4.taskfarm.user.domain.ai.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;

        System.out.println("Gemini model = " + model);

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public int recommendExp(String categoryName, String priority, String title) {
    	String prompt = """
    			당신은 할일(Task)의 난이도와 중요도를 기반으로 적정 경험치(exp)를 산정하는 시스템입니다.

    			다음 할일 정보를 분석하여 사용자에게 부여할 적정 경험치를 계산하세요.

    			[출력 규칙]
    			- 반드시 JSON 객체 하나만 응답하세요.
    			- 형식은 반드시 {"exp": 숫자} 이어야 합니다.
    			- 설명, 문장, 마크다운, 코드블록을 절대 포함하지 마세요.
    			- exp는 반드시 정수여야 합니다.

    			[경험치 정책]
    			우선순위에 따라 기본 XP, 최소 XP, 최대 XP가 다릅니다.

    			- 우선순위 A / HIGH / 높음 / 중요:
    			  - 의미: 중요한 일
    			  - 기본 XP: 30
    			  - 최소 XP: 10
    			  - 최대 XP: 60

    			- 우선순위 B / MEDIUM / 보통 / 일반:
    			  - 의미: 보통 일
    			  - 기본 XP: 15
    			  - 최소 XP: 5
    			  - 최대 XP: 30

    			- 우선순위 C / LOW / 낮음 / 가벼움:
    			  - 의미: 가벼운 일
    			  - 기본 XP: 8
    			  - 최소 XP: 1
    			  - 최대 XP: 15

    			[기본 계산 방식]
    			1. 할일의 우선순위를 먼저 판단하세요.
    			2. 해당 우선순위의 기본 XP에서 시작하세요.
    			3. 카테고리와 제목을 보고 난이도 보정을 적용하세요.
    			4. 최종 exp는 반드시 해당 우선순위의 최소 XP 이상, 최대 XP 이하로 제한하세요.

    			[카테고리 기준]
    			- 공부 / 학습 / 자격증 / 과제: 난이도 높음
    			- 업무 / 프로젝트 / 개발 / 문서작성: 난이도 높음
    			- 운동 / 건강관리: 난이도 보통
    			- 집안일 / 정리 / 청소: 난이도 낮음 또는 보통
    			- 약속 / 일정 / 이동: 난이도 낮음 또는 보통
    			- 취미 / 여가: 난이도 낮음
    			- 기타 / 분류 불명: 제목을 기준으로 판단

    			[제목 기반 난이도 보정]
    			다음 단어가 포함되면 난이도를 높게 판단하세요.
    			- 완성, 구현, 개발, 작성, 발표, 시험, 면접, 배포, 마감, 제출

    			다음 단어가 포함되면 난이도를 보통으로 판단하세요.
    			- 수정, 정리, 복습, 확인, 연습, 회의, 테스트

    			다음 단어가 포함되면 난이도를 낮게 판단하세요.
    			- 보기, 읽기, 검색, 문의, 생각하기, 확인하기

    			[보정 규칙]
    			- 어려운 작업이면 기본 XP보다 높게 책정하세요.
    			- 쉬운 작업이면 기본 XP보다 낮게 책정하세요.
    			- 보통 난이도면 기본 XP 근처로 책정하세요.
    			- 정보가 부족하면 해당 우선순위의 기본 XP를 사용하세요.
    			- 최종 exp가 최소 XP보다 작으면 최소 XP로 제한하세요.
    			- 최종 exp가 최대 XP보다 크면 최대 XP로 제한하세요.
    			- 제공된 카테고리, 우선순위, 제목만 사용하세요.
    			- 확실하지 않은 경우 과도하게 높게 책정하지 말고 기본 XP에 가깝게 책정하세요.

    			[할일 정보]
    			- 카테고리: %s
    			- 우선순위: %s
    			- 제목: %s

    			위 기준에 따라 최종 경험치를 계산하고 JSON만 응답하세요.
    			""".formatted(categoryName, priority, title);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }

        String text = response.candidates()
                .get(0)
                .content()
                .parts()
                .get(0)
                .text();

        return parseExp(text);
    }

    private int parseExp(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Gemini 응답 text가 비어 있습니다.");
        }

        String numberOnly = text.replaceAll("[^0-9]", "");

        if (numberOnly.isBlank()) {
            throw new IllegalArgumentException("Gemini 응답에서 exp를 찾을 수 없습니다. response=" + text);
        }

        return Integer.parseInt(numberOnly);
    }

    public record GeminiResponse(List<Candidate> candidates) {
    }

    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }
}