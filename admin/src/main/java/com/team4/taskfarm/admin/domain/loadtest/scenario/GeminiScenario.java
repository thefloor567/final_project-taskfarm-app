package com.team4.taskfarm.admin.domain.loadtest.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component("GEMINI")
@RequiredArgsConstructor
public class GeminiScenario implements LoadScenario {

  private static final String QUEUE_KEY = "ai:recommend:queue";

  private final Environment env;
  private final ObjectMapper objectMapper;
  private final JdbcTemplate jdbcTemplate;

  @Override
  public void runOnce() throws Exception {
    Long userId = findAnyUserId();
    Long todoId = findAnyTodoId(userId);

    String jobId = UUID.randomUUID().toString();

    Map<String, Object> job = Map.of(
        "jobId", jobId,
        "idxUser", userId,
        "idxTodo", todoId,
        "categoryName", "부하테스트",
        "priority", "B",
        "title", "Gemini load test " + jobId);

    String json = objectMapper.writeValueAsString(job);

    RedisClient redis = new RedisClient(
        get("REDIS_HOST", ""),
        Integer.parseInt(get("REDIS_PORT", "6379")),
        get("REDIS_USERNAME", ""),
        get("REDIS_PASSWORD", ""),
        Boolean.parseBoolean(get("REDIS_SSL", "false")));

    redis.rpush(QUEUE_KEY, json);
  }

  private Long findAnyUserId() {
    Long userId = jdbcTemplate.query(
        "SELECT Idx_User FROM tbUser WHERE DeleteDate IS NULL AND Status = 'ACTIVE' ORDER BY Idx_User LIMIT 1",
        rs -> rs.next() ? rs.getLong(1) : null);

    if (userId == null) {
      throw CustomException.badRequest("GEMINI 시나리오 실행용 활성 유저를 찾을 수 없습니다.");
    }

    return userId;
  }

  private Long findAnyTodoId(Long userId) {
    Long todoId = jdbcTemplate.query(
        "SELECT Idx_Todo FROM tbTodo WHERE Idx_User = ? AND DeleteDate IS NULL ORDER BY Idx_Todo DESC LIMIT 1",
        rs -> rs.next() ? rs.getLong(1) : null,
        userId);

    return todoId != null ? todoId : 0L;
  }

  private String get(String key, String defaultValue) {
    String value = env.getProperty(key);
    if (value == null || value.isBlank()) {
      value = System.getenv(key);
    }
    return value == null || value.isBlank() ? defaultValue : value;
  }

  private static class RedisClient {
    private final String host;
    private final int port;
    private final String username;
    private final String redisAuthValue;
    private final boolean ssl;

    RedisClient(String host, int port, String username, String redisAuthValue, boolean ssl) {
      this.host = host;
      this.port = port;
      this.username = username;
      this.redisAuthValue = redisAuthValue;
      this.ssl = ssl;
    }

    void rpush(String key, String value) throws IOException {
      if (host == null || host.isBlank()) {
        throw CustomException.badRequest("REDIS_HOST가 설정되어 있지 않습니다.");
      }

      try (Socket socket = createSocket();
          BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
          BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

        socket.setSoTimeout((int) Duration.ofSeconds(5).toMillis());

        if (redisAuthValue != null && !redisAuthValue.isBlank()) {
          if (username != null && !username.isBlank()) {
            send(out, "AUTH", username, redisAuthValue);
          } else {
            send(out, "AUTH", redisAuthValue);
          }
          readReply(in);
        }

        send(out, "RPUSH", key, value);
        readReply(in);
      }
    }

    private Socket createSocket() throws IOException {
      if (ssl) {
        return SSLSocketFactory.getDefault().createSocket(host, port);
      }
      return new Socket(host, port);
    }

    private void send(OutputStream out, String... args) throws IOException {
      StringBuilder sb = new StringBuilder();
      sb.append("*").append(args.length).append("\r\n");
      for (String arg : args) {
        byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
        sb.append("$").append(bytes.length).append("\r\n");
        sb.append(arg).append("\r\n");
      }
      out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
      out.flush();
    }

    private void readReply(InputStream in) throws IOException {
      int first = in.read();
      if (first == -1) {
        throw CustomException.badRequest("Redis 응답이 없습니다.");
      }

      String line = readLine(in);
      if (first == '-') {
        throw CustomException.badRequest("Redis 응답 오류: " + line);
      }
    }

    private String readLine(InputStream in) throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int prev = -1;
      int curr;

      while ((curr = in.read()) != -1) {
        if (prev == '\r' && curr == '\n') {
          byte[] bytes = buffer.toByteArray();
          return new String(bytes, 0, Math.max(0, bytes.length - 1), StandardCharsets.UTF_8);
        }
        buffer.write(curr);
        prev = curr;
      }

      throw CustomException.badRequest("Redis 응답 라인 읽기 실패");
    }
  }
}