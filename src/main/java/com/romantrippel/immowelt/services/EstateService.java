package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.dto.EstateResponse;
import com.romantrippel.immowelt.entities.EstateEntity;
import com.romantrippel.immowelt.entities.EstateHistoryEntity;
import com.romantrippel.immowelt.repositories.EstateHistoryRepository;
import com.romantrippel.immowelt.repositories.EstateRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EstateService {

  private final EstateRepository estateRepository;
  private final EstateHistoryRepository estateHistoryRepository;
  private final WebScraper webScraper;
  private final TelegramService telegramService;
  private final ExecutorService executor;

  private final List<Integer> allowedRoomCounts;

  public EstateService(
      @Value("#{'${ESTATE_FILTER_ROOM_COUNTS}'.split(',')}") String[] roomCountsRaw,
      WebScraper webScraper,
      TelegramService telegramService,
      EstateRepository estateRepository,
      EstateHistoryRepository estateHistoryRepository,
      ExecutorService executor) {

    this.allowedRoomCounts =
        Arrays.stream(roomCountsRaw).map(String::trim).map(Integer::parseInt).toList();

    this.webScraper = webScraper;
    this.telegramService = telegramService;
    this.estateRepository = estateRepository;
    this.estateHistoryRepository = estateHistoryRepository;
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

    for (EstateResponse.EstateDto dto : estateDtoList) {

      if (!allowedRoomCounts.contains(dto.rooms())) continue;

      EstateEntity incoming = EstateEntity.fromDto(dto);
      incoming.setApartmentLayoutUrl("N/A");

      EstateEntity existing =
          estateRepository.findByGlobalObjectKey(incoming.getGlobalObjectKey()).orElse(null);

      boolean isNew = existing == null;
      boolean sendTelegram = false;

      if (isNew) {
        incoming.setCreatedAt(LocalDateTime.now());
        EstateEntity savedNew = estateRepository.save(incoming);
        sendTelegram = true;
        scheduleTelegram(dto, savedNew, delaySeconds);
        delaySeconds += 2;
        continue;
      }

      Duration duration = Duration.between(existing.getCreatedAt(), LocalDateTime.now());

      if (duration.toDays() > 7) {
        EstateHistoryEntity history = EstateHistoryEntity.fromEstate(existing);
        estateHistoryRepository.save(history);

        existing.setCreatedAt(LocalDateTime.now());

        sendTelegram = true;
      }

      updateExistingEstate(existing, incoming);
      estateRepository.save(existing);

      if (sendTelegram) {
        scheduleTelegram(dto, existing, delaySeconds);
        delaySeconds += 2;
      }
    }
  }

  private void updateExistingEstate(EstateEntity existing, EstateEntity incoming) {
    existing.setHeadline(incoming.getHeadline());
    existing.setEstateType(incoming.getEstateType());
    existing.setExposeUrl(incoming.getExposeUrl());
    existing.setLivingArea(incoming.getLivingArea());
    existing.setImage(incoming.getImage());
    existing.setImageHD(incoming.getImageHD());
    existing.setCity(incoming.getCity());
    existing.setZip(incoming.getZip());
    existing.setShowMap(incoming.isShowMap());
    existing.setStreet(incoming.getStreet());
    existing.setPriceName(incoming.getPriceName());
    existing.setPriceValue(incoming.getPriceValue());
    existing.setRooms(incoming.getRooms());
    existing.setApartmentLayoutUrl(incoming.getApartmentLayoutUrl());
  }

  private void scheduleTelegram(
      EstateResponse.EstateDto dto, EstateEntity saved, int delaySeconds) {
    int currentDelay = delaySeconds;
    executor.submit(
        () -> {
          try {
            Thread.sleep(currentDelay * 1000L);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          log.info("Sending Telegram message for estate: {}", dto.headline());
          telegramService.sendMessage(formatEstateMessage(saved));
        });
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
