package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.dto.EstateResponse;
import com.romantrippel.immowelt.entities.EstateEntity;
import com.romantrippel.immowelt.entities.EstateHistoryEntity;
import com.romantrippel.immowelt.repositories.EstateHistoryRepository;
import com.romantrippel.immowelt.repositories.EstateRepository;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
  private final PdfDownloader pdfDownloader;

  private final List<Integer> allowedRoomCounts;

  public EstateService(
      @Value("#{'${ESTATE_FILTER_ROOM_COUNTS}'.split(',')}") String[] roomCountsRaw,
      WebScraper webScraper,
      TelegramService telegramService,
      EstateRepository estateRepository,
      EstateHistoryRepository estateHistoryRepository,
      PdfDownloader pdfDownloader) {

    this.allowedRoomCounts =
        Arrays.stream(roomCountsRaw).map(String::trim).map(Integer::parseInt).toList();

    this.webScraper = webScraper;
    this.telegramService = telegramService;
    this.estateRepository = estateRepository;
    this.estateHistoryRepository = estateHistoryRepository;
    this.pdfDownloader = pdfDownloader;
  }

  public void processEstates() throws IOException {
    byte[] pdf;
    List<EstateResponse.EstateDto> estateDtoList;

    try {
      estateDtoList = webScraper.doScraping();
    } catch (Exception e) {
      log.error("Error during web scraping", e);
      return;
    }

    for (EstateResponse.EstateDto dto : estateDtoList) {

      if (!allowedRoomCounts.contains(dto.rooms())) continue;

      EstateEntity incoming = EstateEntity.fromDto(dto);

      EstateEntity existing =
          estateRepository.findByGlobalObjectKey(incoming.getGlobalObjectKey()).orElse(null);

      boolean isNew = existing == null;

      if (isNew) {
        pdf = pdfDownloader.downloadPdfFromPage(incoming.getExposeUrl());
        incoming.setAvailability(PdfDownloader.available);

        incoming.setCreatedAt(LocalDateTime.now());
        EstateEntity savedNew = estateRepository.save(incoming);
        scheduleTelegram(savedNew, pdf);
        continue;
      }

      Duration duration = Duration.between(existing.getCreatedAt(), LocalDateTime.now());

      if (duration.toDays() > 7) {
        EstateHistoryEntity history = EstateHistoryEntity.fromEstate(existing);
        estateHistoryRepository.save(history);

        pdf = pdfDownloader.downloadPdfFromPage(existing.getExposeUrl());
        existing.setAvailability(PdfDownloader.available);

        existing.setCreatedAt(LocalDateTime.now());

        scheduleTelegram(existing, pdf);

        updateExistingEstate(existing, incoming);
        estateRepository.save(existing);
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

  private void scheduleTelegram(EstateEntity saved, byte[] pdf) {
    log.info("Scheduling Telegram message for estate: {}", saved.getHeadline());

    telegramService.sendMessageRateLimited(formatEstateMessage(saved), pdf);
  }

  private String formatEstateMessage(EstateEntity estate) {
    return """
        <a href="%s">📸 View photo</a>

        <b>🏠 %s</b>
        <b>📐 Living area:</b> %.2f m²
        <b>💶 Cold rent:</b> %s
        <b>🛏️ Rooms:</b> %d

        <b>🔑 Move-in date:</b> %s

        🔗 %s
        """
        .formatted(
            estate.getImageHD(),
            estate.getHeadline(),
            estate.getLivingArea(),
            estate.getPriceValue(),
            estate.getRooms(),
            estate.getAvailability(),
            estate.getExposeUrl());
  }
}
