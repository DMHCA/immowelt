package com.romantrippel.immowelt.config;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightHolder {

  public final Playwright playwright;
  public final Browser browser;

  public PlaywrightHolder() {
    this.playwright = Playwright.create();
    this.browser =
        playwright
            .chromium()
            .launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(
                        List.of(
                            "--disable-blink-features=AutomationControlled",
                            "--disable-web-security",
                            "--disable-features=IsolateOrigins,site-per-process")));
    System.out.println("Playwright started with one shared browser.");
  }

  @PreDestroy
  public void shutdown() {
    System.out.println("Closing Playwright...");
    try {
      browser.close();
    } catch (Exception ignored) {
    }
    try {
      playwright.close();
    } catch (Exception ignored) {
    }
  }
}
