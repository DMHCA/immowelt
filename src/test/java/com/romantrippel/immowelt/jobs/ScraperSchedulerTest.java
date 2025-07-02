package com.romantrippel.immowelt.jobs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.romantrippel.immowelt.services.EstateService;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScraperSchedulerTest {

  private EstateService estateService;
  private Clock fixedClock;
  private ScraperScheduler scheduler;

  @BeforeEach
  void setup() {
    estateService = mock(EstateService.class);
    // fixed clock set to Wednesday 10:00
    fixedClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 10, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    scheduler = new ScraperScheduler(estateService, fixedClock);
  }

  @Test
  void shouldRun_returnsFalse_onWeekend() {
    Clock weekendClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 6, 10, 0).toInstant(ZoneOffset.UTC),
            ZoneId.of("UTC")); // Saturday
    scheduler = new ScraperScheduler(estateService, weekendClock);

    assertFalse(scheduler.shouldRun());
  }

  @Test
  void shouldRun_returnsFalse_beforeStartHour() {
    Clock earlyClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 5, 59).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    scheduler = new ScraperScheduler(estateService, earlyClock);

    assertFalse(scheduler.shouldRun());
  }

  @Test
  void shouldRun_returnsFalse_afterEndHour() {
    Clock lateClock =
        Clock.fixed(
            LocalDateTime.of(2024, 7, 1, 23, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    scheduler = new ScraperScheduler(estateService, lateClock);

    assertFalse(scheduler.shouldRun());
  }

  @Test
  void shouldRun_returnsTrue_duringWorkingHoursOnWeekday() {
    assertTrue(scheduler.shouldRun());
  }

  @Test
  void scheduledScraping_callsProcessEstates_whenShouldRunTrue() throws InterruptedException {
    // Given a fixed clock in allowed time window (weekday, 7:00)
    Clock fixedClock =
        Clock.fixed(LocalDateTime.of(2025, 7, 1, 7, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

    ScraperScheduler scheduler = new ScraperScheduler(estateService, fixedClock);

    scheduler.scheduledScraping();

    // Wait for async task to complete
    Thread.sleep(4000);

    verify(estateService, times(1)).processEstates();
  }

  @Test
  void scheduledScraping_doesNotCallProcessEstates_whenShouldRunFalse() {
    ScraperScheduler schedulerSpy = Mockito.spy(new ScraperScheduler(estateService, fixedClock));
    doReturn(false).when(schedulerSpy).shouldRun();

    schedulerSpy.scheduledScraping();

    verify(estateService, never()).processEstates();
  }
}
