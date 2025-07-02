package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.dto.EstateResponse;
import com.romantrippel.immowelt.entities.EstateEntity;
import com.romantrippel.immowelt.repositories.EstateRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EstateService {

  private final int roomCount;
  private final WebScraper webScraper;
  private final TelegramService telegramService;
  private final EstateRepository estateRepository;
  private final ExecutorService executor;

  public EstateService(
      @Value("${ESTATE_FILTER_ROOM_COUNT}") int roomCount,
      WebScraper webScraper,
      TelegramService telegramService,
      EstateRepository estateRepository,
      ExecutorService executor) {
    this.roomCount = roomCount;
    this.webScraper = webScraper;
    this.telegramService = telegramService;
    this.estateRepository = estateRepository;
    this.executor = executor;
  }

  public void processEstates() {
    List<EstateResponse.EstateDto> estateDtoList;

    try {
      estateDtoList = webScraper.doScraping();
    } catch (Exception e) {
      log.error("Error during web scraping", e);
      return;
    }

    int delaySeconds = 0;
    for (EstateResponse.EstateDto estateDto : estateDtoList) {
      if (estateDto.rooms() == roomCount || estateDto.rooms() == roomCount + 1) {
        EstateEntity entity = EstateEntity.fromDto(estateDto);
        int insertedRows = estateRepository.insertIfNotExists(entity);

        if (insertedRows > 0) {
          int currentDelay = delaySeconds;
          executor.submit(
              () -> {
                try {
                  Thread.sleep(currentDelay * 1000L);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
                log.info("Sending Telegram message for estate: {}", estateDto.headline());
                telegramService.sendMessage(formatEstateMessage(estateDto));
              });
          delaySeconds += 2;
        }
      }
    }
  }

  private String formatEstateMessage(EstateResponse.EstateDto estate) {
    return estate.imageHD()
        + "\n"
        + estate.headline()
        + "\n"
        + "Price: "
        + estate.priceValue()
        + "\n"
        + "Rooms: "
        + estate.rooms()
        + "\n"
        + "Living area: "
        + estate.livingArea()
        + " m²\n"
        + estate.exposeUrl();
  }
}
