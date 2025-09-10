package com.romantrippel.immowelt.services;

import com.romantrippel.immowelt.dto.EstateResponse;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebScraper {

  private static final List<String> USER_AGENTS =
      List.of(
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Safari/605.1.15",
          "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
          "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
          "Mozilla/5.0 (Linux; Android 14; SM-G990B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Mobile Safari/537.36");

  private static final Random random = new Random();

  private static final String TARGET_URL =
      "https://data.immowelt.de/brokerprofile/brokerprofile-ui/graphql";

  private static final String JSON_BODY =
      """
        {
          "query": "query estateList_query($sort: String!, $cursor: Int, $limit: Int, $brokerId: String!) { estateList(sort: $sort, cursor: $cursor, limit: $limit, brokerId: $brokerId) { data { isNew headline globalObjectKey estateType salesType exposeUrl area livingArea imageCount image imageHD city zip showMap street priceName priceValue rooms isDiamond projektDetailLink projektDetailLinkText projektTitel } pagination { countPagination countTotal nextPage } } }",
          "operationName": "estateList_query",
          "variables": {
            "brokerId": "f6e8fc019a6e4d07a404acb54b4e0247",
            "cursor": 0,
            "limit": 30,
            "sort": "modifiedAt"
          }
        }
        """;

  @Value("${proxy.enabled:false}")
  private boolean proxyEnabled;

  @Value("${proxy.host:}")
  private String proxyHost;

  @Value("${proxy.port:0}")
  private int proxyPort;

  @Value("${scraper.timeout:15000}")
  private int timeout;

  public List<EstateResponse.EstateDto> doScraping() throws Exception {
    String userAgent = getRandomUserAgent();

    try {
      Response response = prepareRequest(userAgent).execute();
      EstateResponse.Root root = EstateResponse.fromJson(response.body());
      return root.data().estateList().data();
    } catch (Exception e) {
      log.error("Error during scraping request", e);
      throw e;
    }
  }

  public String extractGrundrissPdfUrl(String exposeUrl) {
    String userAgent = getRandomUserAgent();
    try {
      Connection connection =
          Jsoup.connect(exposeUrl)
              .method(Connection.Method.GET)
              .userAgent(userAgent)
              .header("Accept", "text/html,application/xhtml+xml")
              .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
              .header("Referer", "https://www.immowelt.de/")
              .timeout(timeout)
              .followRedirects(true)
              .ignoreContentType(true)
              .ignoreHttpErrors(true);

      Response response = connection.execute();
      String body = response.body();

      if (response.statusCode() != 200) {
        log.warn("Got status {} for URL: {}", response.statusCode(), exposeUrl);
      }

      Pattern pattern =
          Pattern.compile(
              "\\\\\"url\\\\\":\\\\\"(https://[^\\\\\"]+?\\.pdf[^\\\\\"]*?)\\\\\",\\\\\"title\\\\\":\\\\\"Grundriss der ME\\\\\"");
      Matcher matcher = pattern.matcher(body);
      if (matcher.find()) {
        return matcher.group(1);
      }

      log.warn("PDF 'Grundriss der ME' not found for URL: {}", exposeUrl);
      return null;

    } catch (Exception e) {
      log.error("Error while extracting PDF from URL: {}", exposeUrl, e);
      return null;
    }
  }

  private Connection prepareRequest(String userAgent) {
    Connection connection =
        Jsoup.connect(TARGET_URL)
            .method(Connection.Method.POST)
            .header("Accept", "application/json, text/plain, */*")
            .header("Content-Type", "application/json")
            .header("Origin", "https://www.immowelt.de")
            .header("Referer", "https://www.immowelt.de/")
            .header("tenant", "immowelt")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "same-site")
            .header("sec-ch-ua", "\"Chromium\";v=\"133\", \"Google Chrome\";v=\"133\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Linux\"")
            .userAgent(userAgent)
            .requestBody(JSON_BODY)
            .ignoreContentType(true)
            .timeout(15000)
            .followRedirects(true);

    // TODO: use paid proxy later
    // if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
    //   connection = connection.proxy(proxyHost, proxyPort);
    //   log.debug("Using proxy {}:{}", proxyHost, proxyPort);
    // }

    return connection;
  }

  private String getRandomUserAgent() {
    return USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
  }
}
