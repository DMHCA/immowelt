package com.romantrippel.immowelt.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.romantrippel.immowelt.config.TelegramProperties;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

class TelegramServiceTest {

  private WebClient webClient;
  private TelegramProperties props;
  private TelegramService service;

  private WebClient.RequestBodyUriSpec uriSpec;
  private WebClient.RequestBodySpec bodySpec;
  private WebClient.RequestHeadersSpec<?> headersSpec;
  private WebClient.ResponseSpec responseSpec;

  @BeforeEach
  void setUp() {
    webClient = mock(WebClient.class);
    props = new TelegramProperties();
    props.setToken("dummyToken");
    props.setChatId("123456");

    uriSpec = mock(WebClient.RequestBodyUriSpec.class);
    bodySpec = mock(WebClient.RequestBodySpec.class);
    headersSpec = mock(WebClient.RequestHeadersSpec.class);
    responseSpec = mock(WebClient.ResponseSpec.class);

    service = new TelegramService(webClient, props);
  }

  @Test
  void sendMessage_successful() {
    when(webClient.post()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(bodySpec);
    when(bodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("OK"));

    service.sendMessage("hello");

    verify(webClient).post();
    verify(uriSpec).uri("/botdummyToken/sendMessage");

    ArgumentCaptor<Map<String, String>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    verify(bodySpec).bodyValue(payloadCaptor.capture());
    assertEquals("hello", payloadCaptor.getValue().get("text"));
  }

  @Test
  void sendMessage_serverError() {
    when(webClient.post()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(bodySpec);
    when(bodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class))
        .thenReturn(Mono.error(new RuntimeException("Telegram down")));

    service.sendMessage("fail");

    verify(webClient).post();
  }

  @Test
  void sendMessage_webClientResponseException() {
    when(webClient.post()).thenReturn(uriSpec);
    when(uriSpec.uri(anyString())).thenReturn(bodySpec);
    when(bodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    WebClientResponseException ex = mock(WebClientResponseException.class);
    when(ex.getRawStatusCode()).thenReturn(500);
    when(ex.getResponseBodyAsString()).thenReturn("Internal Server Error");

    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    service.sendMessage("error");

    verify(webClient).post();
  }
}
