package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.config.TelegramProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

  private final WebClient telegramWebClient;
  private final TelegramProperties props;

  public void sendMessage(String message) {
    String endpoint = "/bot" + props.getToken() + "/sendMessage";
    Map<String, String> payload =
        Map.of("chat_id", props.getChatId(), "text", message, "parse_mode", "HTML");

    telegramWebClient
        .post()
        .uri(endpoint)
        .bodyValue(payload)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            response -> {
              log.error("Telegram responded with error status: {}", response.statusCode());
              return Mono.empty();
            })
        .bodyToMono(String.class)
        .doOnError(
            WebClientResponseException.class,
            e -> {
              log.error(
                  "Telegram API error: status={}, body={}",
                  e.getRawStatusCode(),
                  e.getResponseBodyAsString());
            })
        .doOnError(
            e -> {
              log.error("Unexpected error while sending Telegram message", e);
            })
        .subscribe();
  }
}
