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
                다음 할일의 적정 경험치를 JSON으로만 응답하세요.

                조건:
                - 반드시 {"exp": 숫자} 형식만 응답
                - 설명 문장 금지
                - exp는 정수

                할일 정보:
                - 카테고리: %s
                - 우선순위: %s
                - 제목: %s
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