package com.romantrippel.immowelt.services;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import com.romantrippel.immowelt.config.PlaywrightHolder;
import com.romantrippel.immowelt.config.ProxyProperties;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class PdfDownloader {

  private final ProxyProperties proxyProps;
  private final PlaywrightHolder holder;

  public static String available = "unknown";

  public PdfDownloader(ProxyProperties proxyProps, PlaywrightHolder holder) {
    this.proxyProps = proxyProps;
    this.holder = holder;
  }

  public byte[] downloadPdfFromPage(String exposeUrl) throws IOException {

    BrowserContext context = null;
    Page page = null;

    try {
      Browser.NewContextOptions ctxOptions =
          new Browser.NewContextOptions()
              .setUserAgent(
                  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                      + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
              .setViewportSize(1400, 900)
              .setLocale("de-DE")
              .setTimezoneId("Europe/Berlin");

      if (proxyProps.isEnabled()) {
        Proxy proxy = new Proxy(proxyProps.getHost() + ":" + proxyProps.getPort());
        if (proxyProps.getUsername() != null && proxyProps.getPassword() != null) {
          proxy.setUsername(proxyProps.getUsername());
          proxy.setPassword(proxyProps.getPassword());
        }
        ctxOptions.setProxy(proxy);
      }

      context = holder.browser.newContext(ctxOptions);
      page = context.newPage();

      page.setExtraHTTPHeaders(
          Map.of(
              "Accept-Language", "de-DE,de;q=0.9,en;q=0.8",
              "Sec-Ch-Ua-Platform", "\"Windows\"",
              "Sec-Ch-Ua-Mobile", "?0",
              "Sec-Ch-Ua",
                  "\"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\", \"Not A Brand\";v=\"99\""));

      page.addInitScript(
          "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });");
      page.addInitScript(
          "const gp = WebGLRenderingContext.prototype.getParameter; WebGLRenderingContext.prototype.getParameter = function(p){ if(p===37445)return'Intel Inc.'; if(p===37446)return'Intel Iris OpenGL'; return gp.call(this,p); };");
      page.addInitScript(
          "navigator.permissions.query = () => Promise.resolve({ state: 'prompt' });");

      page.navigate(exposeUrl, new Page.NavigateOptions().setTimeout(45000));
      page.waitForTimeout(7000);

      try {
        available = page.locator("ul.FeaturesPreview li span").first().innerText();
      } catch (Exception ignored) {
        available = "unknown";
      }

      String scriptContent =
          page.locator("script").allInnerTexts().stream()
              .filter(s -> s.contains("__UFRN_LIFECYCLE_SERVERREQUEST__"))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Required JSON script was not found."));

      Pattern pattern =
          Pattern.compile(
              "\\\\\"url\\\\\":\\\\\"(https://[^\\\\\"]+?\\.pdf[^\\\\\"]*?)\\\\\",\\\\\"title\\\\\":\\\\\"Grundriss der ME\\\\\"",
              Pattern.DOTALL);
      Matcher matcher = pattern.matcher(scriptContent);

      if (!matcher.find()) {
        throw new RuntimeException("Unable to extract PDF URL.");
      }

      String pdfUrl = matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");

      return page.request().get(pdfUrl).body();

    } catch (Exception e) {
      throw new IOException("Error while downloading PDF: " + exposeUrl, e);

    } finally {
      if (page != null)
        try {
          page.close();
        } catch (Exception ignore) {
        }
      if (context != null)
        try {
          context.close();
        } catch (Exception ignore) {
        }
    }
  }
}
