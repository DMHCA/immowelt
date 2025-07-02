package com.romantrippel.immowelt.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.romantrippel.immowelt.dto.EstateResponse;
import com.romantrippel.immowelt.entities.EstateEntity;
import com.romantrippel.immowelt.repositories.EstateRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EstateServiceTest {

  private WebScraper webScraper;
  private TelegramService telegramService;
  private EstateRepository estateRepository;
  private ExecutorService executor;

  private EstateService estateService;

  @BeforeEach
  void setup() {
    webScraper = mock(WebScraper.class);
    telegramService = mock(TelegramService.class);
    estateRepository = mock(EstateRepository.class);

    executor = mock(ExecutorService.class);

    doAnswer(
            invocation -> {
              Runnable task = invocation.getArgument(0);
              task.run();
              return CompletableFuture.completedFuture(null);
            })
        .when(executor)
        .submit(any(Runnable.class));

    estateService =
        new EstateService(
            2, // например roomCount = 2
            webScraper,
            telegramService,
            estateRepository,
            executor);
  }

  @Test
  void processEstates_schedulesMessagesForNewEstatesWithCorrectDelay() throws Exception {
    EstateResponse.EstateDto estate1 = mock(EstateResponse.EstateDto.class);
    when(estate1.rooms()).thenReturn(2);
    when(estate1.headline()).thenReturn("Estate 1");
    when(estate1.imageHD()).thenReturn("img1");
    when(estate1.priceValue()).thenReturn("1000");
    when(estate1.livingArea()).thenReturn(Double.valueOf("50"));
    when(estate1.exposeUrl()).thenReturn("url1");

    EstateResponse.EstateDto estate2 = mock(EstateResponse.EstateDto.class);
    when(estate2.rooms()).thenReturn(3);
    when(estate2.headline()).thenReturn("Estate 2");
    when(estate2.imageHD()).thenReturn("img2");
    when(estate2.priceValue()).thenReturn("2000");
    when(estate2.livingArea()).thenReturn(Double.valueOf("70"));
    when(estate2.exposeUrl()).thenReturn("url2");

    when(webScraper.doScraping()).thenReturn(List.of(estate1, estate2));

    when(estateRepository.insertIfNotExists(any(EstateEntity.class))).thenReturn(1);

    estateService.processEstates();

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(telegramService, times(2)).sendMessage(messageCaptor.capture());

    List<String> messages = messageCaptor.getAllValues();

    assertTrue(messages.get(0).contains("Estate 1"));
    assertTrue(messages.get(1).contains("Estate 2"));
  }
}
