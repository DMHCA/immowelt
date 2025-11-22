package com.romantrippel.immowelt.jobs;

import com.romantrippel.immowelt.services.EstateService;
import java.time.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperScheduler {

  private final EstateService estateService;
  private final Clock clock;

  private static final long SCRAPE_INTERVAL_MS = 52_000;
  private static final int START_HOUR = 6;
  private static final int END_HOUR = 23;
  private static final int RANDOM_DELAY_RANGE_SEC = 7;

  @Scheduled(fixedRate = SCRAPE_INTERVAL_MS)
  public void scheduledScraping() {
    if (!shouldRun()) return;

    int delay = ThreadLocalRandom.current().nextInt(RANDOM_DELAY_RANGE_SEC);
    log.debug("Scheduling scraping in {} seconds", delay);

    CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS).execute(this::runScrapingTask);
  }

  private void runScrapingTask() {
    try {
      estateService.processEstates();
    } catch (Exception e) {
      log.error("Error during estate processing", e);
    }
  }

  boolean shouldRun() {
    LocalTime now = LocalTime.now(clock);
    DayOfWeek day = LocalDate.now(clock).getDayOfWeek();

    return !(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)
        && now.getHour() >= START_HOUR
        && now.getHour() <= END_HOUR;
  }
}
