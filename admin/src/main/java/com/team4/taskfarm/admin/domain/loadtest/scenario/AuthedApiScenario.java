package com.team4.taskfarm.admin.domain.loadtest.scenario;

import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component("AUTHED_API")
@RequiredArgsConstructor
public class AuthedApiScenario implements LoadScenario {

  private final Environment env;

  private final HttpClient httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(3))
      .build();


  @Override
  public void runOnce() throws Exception {
    throw CustomException.badRequest("AUTHED_API 시나리오는 액세스 토큰이 필요합니다. 실행 시 토큰을 입력하세요.");
  }

  @Override
  public void runOnce(String accessToken) throws Exception {
    if (accessToken == null || accessToken.isBlank()) {
      throw CustomException.badRequest("액세스 토큰이 비어 있습니다. 정상 로그인(구글 MFA 통과) 후 받은 토큰을 입력하세요.");
    }

    String baseUrl = get("LOADTEST_USER_BASE_URL", "http://taskfarm-user:80");

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/home"))
        .timeout(Duration.ofSeconds(5))
        .header("Authorization", "Bearer " + accessToken)
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    int status = response.statusCode();

    if (status == 401 || status == 403) {
      throw CustomException.badRequest(
          "인증 실패(status=" + status + "). 토큰이 만료됐거나 무효합니다. 토큰을 새로 발급받아 다시 입력하세요.");
    }

    if (status < 200 || status >= 300) {
      throw CustomException.badRequest("AUTHED_API 시나리오 실패. status=" + status);
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