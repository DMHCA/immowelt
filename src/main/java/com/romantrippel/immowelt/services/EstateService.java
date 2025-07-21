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

  private final List<Integer> allowedRoomCounts;
  private final WebScraper webScraper;
  private final TelegramService telegramService;
  private final EstateRepository estateRepository;
  private final ExecutorService executor;

  public EstateService(
      @Value("#{'${ESTATE_FILTER_ROOM_COUNTS}'.split(',')}") List<String> roomCountsRaw,
      WebScraper webScraper,
      TelegramService telegramService,
      EstateRepository estateRepository,
      ExecutorService executor) {
    this.allowedRoomCounts =
        roomCountsRaw.stream().map(String::trim).map(Integer::parseInt).toList();
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
      if (allowedRoomCounts.contains(estateDto.rooms())) {
        EstateEntity entity = EstateEntity.fromDto(estateDto);

        var layoutUrl = webScraper.extractGrundrissPdfUrl(estateDto.exposeUrl());
        entity.setApartmentLayoutUrl(layoutUrl);

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
                telegramService.sendMessage(formatEstateMessage(entity));
              });
          delaySeconds += 2;
        }
      }
    }
  }

  private String formatEstateMessage(EstateEntity estate) {
    return """
    <a href="%s">📸 View photo</a>

    <b>🏠 %s</b>
    <b>📐 Living area:</b> %.2f m²
    <b>💶 Cold rent:</b> %s
    <b>🛏️ Rooms:</b> %d

    <a href="%s">📄 View apartment layout</a>

    🔗 %s
    """
        .formatted(
            estate.getImage(),
            estate.getHeadline(),
            estate.getLivingArea(),
            estate.getPriceValue(),
            estate.getRooms(),
            estate.getApartmentLayoutUrl(),
            estate.getExposeUrl());
  }
}
