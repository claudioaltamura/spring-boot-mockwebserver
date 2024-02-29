package de.claudioaltamura.spring.boot.mockwebserver;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  void shouldReturnWhenUserById() throws InterruptedException {
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

  @Test
  void shouldReturn201WhenCreateUser() {
    var mockResponse =
        new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("{\"id\": 1, \"name\":\"Rocket\"}")
            .throttleBody(16, 5, TimeUnit.SECONDS)
            .setResponseCode(201);

    mockWebServer.enqueue(mockResponse);

    JsonNode result =
        userClient.createNewUser(new ObjectMapper().createObjectNode().put("name", "Rocket"));

    assertEquals(1, result.get("id").asInt());
    assertEquals("Rocket", result.get("name").asText());
  }

  @Test
  void shouldThrowExceptionWhenCreateUser() {
    var mockResponse = new MockResponse().setResponseCode(204);

    mockWebServer.enqueue(mockResponse);

    var objectNode = new ObjectMapper().createObjectNode().put("name", "Rocket");
    assertThrows(
        RuntimeException.class,
        () -> userClient.createNewUser(objectNode)
    );
  }
}
