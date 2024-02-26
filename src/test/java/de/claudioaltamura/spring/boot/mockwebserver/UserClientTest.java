package de.claudioaltamura.spring.boot.mockwebserver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class UserClientTest {

  private MockWebServer mockWebServer;
  private UserClient userClient;

  @BeforeEach
  public void setup() throws IOException {
    this.mockWebServer = new MockWebServer();
    this.mockWebServer.start();
    this.userClient = new UserClient(WebClient.builder(), mockWebServer.url("/").toString());
  }

  @Test
  void testGetUserById() throws InterruptedException {
    var mockResponse =
        new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("{\"id\": 1, \"name\":\"Rocket\"}")
            .throttleBody(16, 5, TimeUnit.SECONDS);

    mockWebServer.enqueue(mockResponse);

    var result = userClient.getUserById(1L);

    assertEquals(1, result.get("id").asInt());
    assertEquals("Rocket", result.get("name").asText());

    var request = mockWebServer.takeRequest();
    assertEquals("/users/1", request.getPath());
  }
}
