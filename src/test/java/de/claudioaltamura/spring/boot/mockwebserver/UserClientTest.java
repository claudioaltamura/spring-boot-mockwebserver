package de.claudioaltamura.spring.boot.mockwebserver;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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
        MockResponse mockResponse =
                new MockResponse()
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .setBody("{\"id\": 1, \"name\":\"Rocket\"}")
                        .throttleBody(16, 5, TimeUnit.SECONDS);

        mockWebServer.enqueue(mockResponse);

        JsonNode result = userClient.getUserById(1L);

        assertEquals(1, result.get("id").asInt());
        assertEquals("Rocket", result.get("name").asText());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/users/1", request.getPath());
    }

}