package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.dto.EstateResponse;
import com.romantrippel.immowelt.entities.EstateEntity;
import com.romantrippel.immowelt.repositories.EstateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstateService {

    private final WebScraper webScraper;
    private final TelegramService telegramService;
    private final EstateRepository estateRepository;

    public EstateService(WebScraper webScraper, TelegramService telegramService, EstateRepository estateRepository) {
        this.webScraper = webScraper;
        this.telegramService = telegramService;
        this.estateRepository = estateRepository;
    }

    public void processEstates() {
        int roomCount = 3;
        List<EstateResponse.EstateDto> estateDtoList;

        try {
            estateDtoList = webScraper.doScraping();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        estateDtoList.
                stream().filter(estateDto -> estateDto.rooms() == roomCount).
                forEach(estateDto -> {
                    EstateEntity entity = EstateEntity.fromDto(estateDto);
                    int insertedRows = estateRepository.insertIfNotExists(
                            entity.getGlobalObjectKey(),
                            entity.getHeadline(),
                            entity.getEstateType(),
                            entity.getExposeUrl(),
                            entity.getLivingArea(),
                            entity.getImage(),
                            entity.getImageHD(),
                            entity.getCity(),
                            entity.getZip(),
                            entity.isShowMap(),
                            entity.getStreet(),
                            entity.getPriceName(),
                            entity.getPriceValue(),
                            entity.getRooms(),
                            entity.getCreatedAt()
                    );

                    if (insertedRows > 0) {
                        System.out.println("Sending Telegram message for estate: " + estateDto.headline());
                        telegramService.sendMessage(formatEstateMessage(estateDto));
                    }

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while sleeping", e);
                    }
                });

    }

    private String formatEstateMessage(EstateResponse.EstateDto estate) {
        return estate.imageHD() + "\n" +
                estate.headline() + "\n" +
                "Price: " + estate.priceValue() + "\n" +
                "Rooms: " + estate.rooms() + "\n" +
                "Living area: " + estate.livingArea() + " m²\n" +
                estate.exposeUrl();
    }
}
