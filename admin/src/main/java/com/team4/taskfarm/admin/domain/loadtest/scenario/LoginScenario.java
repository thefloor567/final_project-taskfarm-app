package com.team4.taskfarm.admin.domain.loadtest.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component("LOGIN")
@RequiredArgsConstructor
public class LoginScenario implements LoadScenario {

  private final Environment env;
  private final ObjectMapper objectMapper;

  private final HttpClient httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(3))
      .build();

  @Override
  public void runOnce() throws Exception {
    String baseUrl = get("LOADTEST_USER_BASE_URL", "http://taskfarm-user:80");
    String email = get("LOADTEST_LOGIN_EMAIL", "");
    String loginPassword = get("LOADTEST_LOGIN_PASSWORD", "");

    if (email.isBlank() || loginPassword.isBlank()) {
      throw new IllegalStateException("LOADTEST_LOGIN_EMAIL / LOADTEST_LOGIN_PASSWORD 설정이 필요합니다.");
    }

    String body = objectMapper.writeValueAsString(Map.of(
        "email", email,
        "password", loginPassword));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/auth/login"))
        .timeout(Duration.ofSeconds(5))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException("LOGIN 시나리오 실패. status=" + response.statusCode());
    }
  }

  private String get(String key, String defaultValue) {
    String value = env.getProperty(key);
    if (value == null || value.isBlank()) {
      value = System.getenv(key);
    }
    return value == null || value.isBlank() ? defaultValue : value;
  }
}