package de.claudioaltamura.spring.boot.mockwebserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserClient {

  private final WebClient webClient;

  public UserClient(WebClient.Builder builder, @Value("${client.user.url}") String usersBaseUrl) {
    this.webClient = builder.baseUrl(usersBaseUrl).build();
  }

  public JsonNode getUserById(Long id) {
    return this.webClient
        .get()
        .uri("/users/{id}", id)
        .retrieve()
        .bodyToMono(JsonNode.class)
        .block();
  }

  public JsonNode createNewUser(JsonNode payload) {
    return this.webClient
        .post()
        .uri("/users")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(payload)
        .retrieve()
        .onStatus(
            httpStatus -> httpStatus != HttpStatus.CREATED,
            clientResponse ->
                clientResponse
                    .createException()
                    .flatMap(
                        it ->
                            Mono.error(
                                new RuntimeException("code : " + clientResponse.statusCode()))))
        .bodyToMono(JsonNode.class)
        .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable)))
        .block();
  }
}
