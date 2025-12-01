package com.romantrippel.immowelt.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.romantrippel.immowelt.dto.EstateResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class WebScraper {

  private final ObjectMapper objectMapper;

  private static final List<String> USER_AGENTS =
      List.of(
          "Mozilla/5.0 (Windows NT 10.0; Win32; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Safari/605.1.15",
          "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
          "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
          "Mozilla/5.0 (Linux; Android 14; SM-G990B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Mobile Safari/537.36");

  private static final String TARGET_URL =
      "https://data.immowelt.de/brokerprofile/brokerprofile-ui/graphql";

  public List<EstateResponse.EstateDto> doScraping() throws Exception {
    String userAgent = getRandomUserAgent();

    try {
      String requestJson = buildGraphQlBody();

      Response response = prepareRequest(userAgent, requestJson).execute();

      EstateResponse.Root root = objectMapper.readValue(response.body(), EstateResponse.Root.class);

      return root.data().estateList().data();

    } catch (Exception e) {
      log.error("Error during scraping request", e);
      throw e;
    }
  }

  /** Создаёт корректное JSON-тело GraphQL-запроса */
  private String buildGraphQlBody() throws Exception {
    Map<String, Object> payload = new HashMap<>();

    String graphqlQuery =
        "query estateList_query($sort: String!, $cursor: Int, $limit: Int, $brokerId: String!) { "
            + "estateList(sort: $sort, cursor: $cursor, limit: $limit, brokerId: $brokerId) { "
            + "data { isNew headline globalObjectKey estateType salesType exposeUrl area livingArea "
            + "imageCount image imageHD city zip showMap street priceName priceValue rooms "
            + "isDiamond projektDetailLink projektDetailLinkText projektTitel } "
            + "pagination { countPagination countTotal nextPage } "
            + "} }";

    payload.put("query", graphqlQuery);
    payload.put("operationName", "estateList_query");

    Map<String, Object> vars =
        Map.of(
            "brokerId",
            "f6e8fc019a6e4d07a404acb54b4e0247",
            "cursor",
            0,
            "limit",
            30,
            "sort",
            "modifiedAt");

    payload.put("variables", vars);

    return objectMapper.writeValueAsString(payload);
  }

  private Connection prepareRequest(String userAgent, String requestBodyJson) {
    return Jsoup.connect(TARGET_URL)
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
        .requestBody(requestBodyJson)
        .ignoreContentType(true)
        .timeout(15000)
        .followRedirects(true);
  }

  private String getRandomUserAgent() {
    int index = ThreadLocalRandom.current().nextInt(USER_AGENTS.size());
    return USER_AGENTS.get(index);
  }
}
