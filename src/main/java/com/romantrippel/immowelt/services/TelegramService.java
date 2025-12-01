package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.config.TelegramProperties;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
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

  private final ExecutorService rateLimiter = Executors.newSingleThreadExecutor();
  private static final int DELAY_SECONDS = 2;

  public void sendMessageRateLimited(String formattedMessage, byte[] pdf) {
    rateLimiter.submit(
        () -> {
          try {
            sendMessage(formattedMessage);

            if (pdf != null && pdf.length > 0) {
              sendDocument(pdf);
            }

            TimeUnit.SECONDS.sleep(DELAY_SECONDS);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate-limited message interrupted", e);
          }
        });
  }

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

  public void sendDocument(byte[] fileBytes) {
    String endpoint = "/bot" + props.getToken() + "/sendDocument";

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("chat_id", props.getChatId());
    builder.part("caption", "Apartment layout");
    builder.part(
        "document",
        new ByteArrayResource(fileBytes) {
          @Override
          public String getFilename() {
            return "Apartment_layout.pdf";
          }
        });

    telegramWebClient
        .post()
        .uri(endpoint)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .bodyValue(builder.build())
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(e -> log.error("Failed to send document", e))
        .subscribe();
  }
}
