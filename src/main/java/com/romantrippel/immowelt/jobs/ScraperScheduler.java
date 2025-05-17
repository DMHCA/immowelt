package com.romantrippel.immowelt.jobs;

import com.romantrippel.immowelt.services.EstateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Random;

@Component
public class ScraperScheduler {

    private final EstateService estateService;

    public ScraperScheduler(EstateService estateService) {
        this.estateService = estateService;
    }

    @Scheduled(fixedRate = 15000)
    public void scheduledScraping() throws Exception {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (hour >= 6 && hour <= 22) {
            int delay = 12 + new Random().nextInt(5);
            Thread.sleep(delay * 1000L);

            System.out.printf("📡 Starting scraping in %s sec...", delay);
            estateService.processEstates();
        }
    }
}