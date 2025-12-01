package com.romantrippel.immowelt.jobs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.romantrippel.immowelt.services.EstateService;
import java.io.IOException;
import java.time.*;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScraperSchedulerTest {

  @Test
  void shouldRun_returnsFalse_onWeekend() {
    EstateService estateService = mock(EstateService.class);
    Clock weekendClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 6, 10, 0) // Saturday
                .toInstant(ZoneOffset.ofHours(2)),
            ZoneId.of("Europe/Berlin"));
    ExecutorService executor = mock(ExecutorService.class);

    ScraperScheduler scheduler = new ScraperScheduler(estateService, weekendClock, executor);

    assertFalse(scheduler.shouldRun());
  }

  @Test
  void shouldRun_returnsFalse_beforeStartHour() {
    EstateService estateService = mock(EstateService.class);
    Clock earlyClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 5, 59) // before 6:00
                .toInstant(ZoneOffset.ofHours(2)),
            ZoneId.of("Europe/Berlin"));
    ExecutorService executor = mock(ExecutorService.class);

    ScraperScheduler scheduler = new ScraperScheduler(estateService, earlyClock, executor);

    assertFalse(scheduler.shouldRun());
  }

  @Test
  void shouldRun_returnsFalse_afterEndHour() {
    EstateService estateService = mock(EstateService.class);
    Clock lateClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 23, 1) // after 23:00
                .toInstant(ZoneOffset.ofHours(2)),
            ZoneId.of("Europe/Berlin"));
    ExecutorService executor = mock(ExecutorService.class);

    ScraperScheduler scheduler = new ScraperScheduler(estateService, lateClock, executor);

    assertFalse(scheduler.shouldRun());
  }

  @Test
  void shouldRun_returnsTrue_duringWorkingHoursOnWeekday() {
    EstateService estateService = mock(EstateService.class);
    Clock weekdayClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 10, 0) // Wednesday 10:00
                .toInstant(ZoneOffset.ofHours(2)),
            ZoneId.of("Europe/Berlin"));
    ExecutorService executor = mock(ExecutorService.class);

    ScraperScheduler scheduler = new ScraperScheduler(estateService, weekdayClock, executor);

    assertTrue(scheduler.shouldRun());
  }

  @Test
  void scheduledScraping_callsProcessEstates_whenShouldRunTrue() throws IOException {
    EstateService estateService = mock(EstateService.class);
    Clock fixedClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 7, 1) // during work hours
                .toInstant(ZoneOffset.ofHours(2)),
            ZoneId.of("Europe/Berlin"));
    ExecutorService executor = mock(ExecutorService.class);

    ScraperScheduler scheduler = new ScraperScheduler(estateService, fixedClock, executor);

    scheduler.scheduledScraping();

    // Проверяем, что задача была передана executor
    verify(executor, times(1)).submit(any(Runnable.class));
  }

  @Test
  void scheduledScraping_doesNotCallProcessEstates_whenShouldRunFalse() throws IOException {
    EstateService estateService = mock(EstateService.class);
    Clock fixedClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 6, 10, 0) // Saturday
                .toInstant(ZoneOffset.ofHours(2)),
            ZoneId.of("Europe/Berlin"));
    ExecutorService executor = mock(ExecutorService.class);

    ScraperScheduler scheduler =
        Mockito.spy(new ScraperScheduler(estateService, fixedClock, executor));
    doReturn(false).when(scheduler).shouldRun();

    scheduler.scheduledScraping();

    verify(executor, never()).submit(any(Runnable.class));
  }
}
