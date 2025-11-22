package com.romantrippel.immowelt.services;

import static org.mockito.Mockito.*;

import com.romantrippel.immowelt.dto.EstateResponse.EstateDto;
import com.romantrippel.immowelt.entities.EstateEntity;
import com.romantrippel.immowelt.entities.EstateHistoryEntity;
import com.romantrippel.immowelt.repositories.EstateHistoryRepository;
import com.romantrippel.immowelt.repositories.EstateRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstateServiceTest {

  private EstateRepository estateRepository;
  private EstateHistoryRepository estateHistoryRepository;
  private TelegramService telegramService;
  private ExecutorService executor;
  private WebScraper webScraper;
  private EstateService estateService;

  @BeforeEach
  void setUp() {
    estateRepository = mock(EstateRepository.class);
    estateHistoryRepository = mock(EstateHistoryRepository.class);
    telegramService = mock(TelegramService.class);
    executor = mock(ExecutorService.class);
    webScraper = mock(WebScraper.class);

    estateService =
        new EstateService(
            new String[] {"1", "2", "3"},
            webScraper,
            telegramService,
            estateRepository,
            estateHistoryRepository,
            executor);
  }

  @Test
  void testNewEstate_savesAndSendsTelegram() throws Exception {
    EstateDto dto =
        new EstateDto(
            "headline1", // headline
            "key1", // globalObjectKey
            "Apartment", // estateType
            "url", // exposeUrl
            50, // livingArea
            "img", // image
            "imgHD", // imageHD
            "city", // city
            "zip", // zip
            true, // showMap
            "street", // street
            "priceName", // priceName
            "1000", // priceValue
            2 // rooms
            );

    when(webScraper.doScraping()).thenReturn(List.of(dto));
    when(estateRepository.findByGlobalObjectKey("key1")).thenReturn(java.util.Optional.empty());
    when(estateRepository.save(any(EstateEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    estateService.processEstates();

    verify(estateRepository).save(any(EstateEntity.class));
    verify(telegramService).sendMessage(anyString());
  }

  @Test
  void testExistingEstateOlderThan7Days_updatesHistoryAndTelegram() throws Exception {
    EstateDto dto =
        new EstateDto(
            "headline2",
            "key2",
            "Apartment",
            "url",
            50,
            "img",
            "imgHD",
            "city",
            "zip",
            true,
            "street",
            "priceName",
            "1000",
            2);

    EstateEntity existing = EstateEntity.fromDto(dto);
    existing.setCreatedAt(LocalDateTime.now().minusDays(10));

    when(webScraper.doScraping()).thenReturn(List.of(dto));
    when(estateRepository.findByGlobalObjectKey("key2"))
        .thenReturn(java.util.Optional.of(existing));
    when(estateRepository.save(any(EstateEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(estateHistoryRepository.save(any(EstateHistoryEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    estateService.processEstates();

    verify(estateRepository).save(existing);
    verify(estateHistoryRepository).save(any(EstateHistoryEntity.class));
    verify(telegramService).sendMessage(anyString());
  }

  @Test
  void testExistingEstateYoungerThan7Days_updatesWithoutHistory() throws Exception {
    EstateDto dto =
        new EstateDto(
            "headline3",
            "key3",
            "Apartment",
            "url",
            50,
            "img",
            "imgHD",
            "city",
            "zip",
            true,
            "street",
            "priceName",
            "1000",
            2);

    EstateEntity existing = EstateEntity.fromDto(dto);
    existing.setCreatedAt(LocalDateTime.now().minusDays(3));

    when(webScraper.doScraping()).thenReturn(List.of(dto));
    when(estateRepository.findByGlobalObjectKey("key3"))
        .thenReturn(java.util.Optional.of(existing));
    when(estateRepository.save(any(EstateEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    estateService.processEstates();

    verify(estateRepository).save(existing);
    verify(estateHistoryRepository, never()).save(any());
    verify(telegramService).sendMessage(anyString());
  }
}
