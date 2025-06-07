package com.romantrippel.immowelt.services;

import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {

  private final RestTemplate restTemplate = new RestTemplate();
  private final String token = System.getenv("TELEGRAM_TOKEN");
  private final String chatId = System.getenv("TELEGRAM_CHAT_ID");

  public void sendMessage(String message) {
    String url = "https://api.telegram.org/bot" + token + "/sendMessage";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> body =
        Map.of(
            "chat_id", chatId,
            "text", message);

    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
      if (!response.getStatusCode().is2xxSuccessful()) {
        System.err.println("Telegram error: " + response.getBody());
      }
    } catch (Exception e) {
      System.err.println("Failed to send Telegram message: " + e.getMessage());
    }
  }
}
