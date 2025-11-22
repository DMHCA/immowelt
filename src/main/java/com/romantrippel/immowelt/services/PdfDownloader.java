package com.romantrippel.immowelt.services;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import com.romantrippel.immowelt.config.ProxyProperties;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PdfDownloader {

  private final ProxyProperties proxyProps;
  static String available;

  @Autowired
  public PdfDownloader(ProxyProperties proxyProps) {
    this.proxyProps = proxyProps;
  }

  /**
   * Downloads the "Grundriss der ME" PDF from the expose page.
   *
   * @param exposeUrl the immowelt expose URL
   * @return PDF bytes
   */
  public byte[] downloadPdfFromPage(String exposeUrl) throws IOException {
    try (Playwright playwright = Playwright.create()) {

      BrowserType.LaunchOptions options =
          new BrowserType.LaunchOptions()
              .setHeadless(true)
              .setArgs(
                  List.of(
                      "--disable-blink-features=AutomationControlled",
                      "--disable-web-security",
                      "--disable-features=IsolateOrigins,site-per-process,site-instance-groups",
                      "--disable-gpu",
                      "--disable-infobars",
                      "--disable-dev-shm-usage",
                      "--window-size=1400,900"));

      if (proxyProps.isEnabled()) {
        Proxy proxy = new Proxy(proxyProps.getHost() + ":" + proxyProps.getPort());
        if (proxyProps.getUsername() != null && proxyProps.getPassword() != null) {
          proxy.setUsername(proxyProps.getUsername());
          proxy.setPassword(proxyProps.getPassword());
        }
        options.setProxy(proxy);
      }

      Browser browser = playwright.chromium().launch(options);

      // --- Stealth scripts ---
      String removeWebDriver =
          "Object.defineProperty(navigator, 'webdriver', { get: () -> undefined });";

      String fakeWebGL =
          "const getParameter = WebGLRenderingContext.prototype.getParameter;"
              + "WebGLRenderingContext.prototype.getParameter = function(param) {"
              + "if(param===37445)return'Intel Inc.';"
              + "if(param===37446)return'Intel Iris OpenGL';"
              + "return getParameter.call(this,param);"
              + "};";

      String normalizePermissions =
          "const originalQuery = window.navigator.permissions.query;"
              + "window.navigator.permissions.query = (parameters) => {"
              + "return Promise.resolve({ state: 'prompt' });"
              + "};";

      BrowserContext context =
          browser.newContext(
              new Browser.NewContextOptions()
                  .setUserAgent(
                      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                          + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                  .setViewportSize(1400, 900)
                  .setLocale("de-DE")
                  .setTimezoneId("Europe/Berlin"));

      Page page = context.newPage();

      page.setExtraHTTPHeaders(
          Map.of(
              "Accept-Language",
              "de-DE,de;q=0.9,en;q=0.8",
              "Sec-Ch-Ua-Platform",
              "\"Windows\"",
              "Sec-Ch-Ua-Mobile",
              "?0",
              "Sec-Ch-Ua",
              "\"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\", \"Not A Brand\";v=\"99\""));

      page.addInitScript(removeWebDriver);
      page.addInitScript(fakeWebGL);
      page.addInitScript(normalizePermissions);

      page.navigate(exposeUrl, new Page.NavigateOptions().setTimeout(45_000));
      page.waitForTimeout(7000); // ждем 7 секунд динамического контента

      // Minimal human-like movement
      page.mouse().move(120, 160);
      page.mouse().move(250, 340);
      page.mouse().move(400, 500);

      try {
        available = page.locator("ul.FeaturesPreview li span").first().innerText();
      } catch (Exception ignored) {
        available = "unknown";
      }

      String scriptContent =
          page.locator("script").allInnerTexts().stream()
              .filter(s -> s.contains("__UFRN_LIFECYCLE_SERVERREQUEST__"))
              .findFirst()
              .orElseThrow(
                  () -> new RuntimeException("Required JSON script was not found on the page."));

      Pattern pattern =
          Pattern.compile(
              "\\\\\"url\\\\\":\\\\\"(https://[^\\\\\"]+?\\.pdf[^\\\\\"]*?)\\\\\",\\\\\"title\\\\\":\\\\\"Grundriss der ME\\\\\"",
              Pattern.DOTALL);

      Matcher matcher = pattern.matcher(scriptContent);
      if (!matcher.find()) {
        throw new RuntimeException("Unable to extract PDF URL from script.");
      }

      String pdfUrl = matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");

      return page.request().get(pdfUrl).body();

    } catch (Exception e) {
      throw new IOException("Error while downloading PDF from: " + exposeUrl, e);
    }
  }
}
