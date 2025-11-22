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
      incoming.setApartmentLayoutUrl("N/A"); // TODO: extract layout later

      boolean isNew =
          estateRepository.findByGlobalObjectKey(incoming.getGlobalObjectKey()).isEmpty();
      EstateEntity saved = saveOrUpdate(incoming);

      boolean sendTelegram = false;

      if (isNew) {
        sendTelegram = true;
      } else {
        Duration duration = Duration.between(saved.getCreatedAt(), LocalDateTime.now());
        if (duration.toDays() > 7) {
          // save history
          EstateHistoryEntity history = EstateHistoryEntity.fromEstate(saved);
          estateHistoryRepository.save(history);
          sendTelegram = true;
        }
      }

      if (sendTelegram) {
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
        delaySeconds += 2;
      }
    }
  }

  private EstateEntity saveOrUpdate(EstateEntity incoming) {
    return estateRepository
        .findByGlobalObjectKey(incoming.getGlobalObjectKey())
        .map(
            existing -> {
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
              return estateRepository.save(existing);
            })
        .orElseGet(() -> estateRepository.save(incoming));
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
