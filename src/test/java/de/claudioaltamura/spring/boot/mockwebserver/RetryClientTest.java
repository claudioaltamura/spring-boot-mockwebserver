package de.claudioaltamura.spring.boot.mockwebserver;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class RetryClientTest {

  private MockWebServer mockWebServer;
  private RetryClient retryClient;

  @BeforeEach
  public void setup() throws IOException {
    this.mockWebServer = new MockWebServer();
    this.mockWebServer.start();
    this.retryClient =
        new RetryClient(WebClient.builder(), mockWebServer.url("/").toString(), new ObjectMapper());
  }

  @Test
  void testRetry() {
    var failureResponse = new MockResponse().setResponseCode(500);

    mockWebServer.enqueue(failureResponse);
    mockWebServer.enqueue(failureResponse);

    var mockResponse =
        new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("{\"id\": 1, \"name\":\"Rocket\"}")
            .throttleBody(16, 5000, TimeUnit.MILLISECONDS);

    mockWebServer.enqueue(mockResponse);

    var result = retryClient.getData();

    assertEquals("empty", result.get("message").asText());
  }
}
