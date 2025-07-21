package com.romantrippel.immowelt.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.romantrippel.immowelt.dto.EstateResponse;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class WebScraperTest {

  private WebScraper webScraper;

  @BeforeEach
  void setUp() {
    webScraper = new WebScraper();
  }

  @Test
  void doScraping_returnsEstateList_whenResponseIsValid() throws Exception {
    String json =
        """
        {
          "data": {
            "estateList": {
              "data": [
                {
                  "headline": "Test Estate",
                  "globalObjectKey": "123",
                  "estateType": "apartment",
                  "salesType": "buy",
                  "exposeUrl": "https://example.com",
                  "city": "Berlin",
                  "zip": "10115",
                  "rooms": 3,
                  "priceName": "Kaufpreis",
                  "priceValue": 350000
                }
              ],
              "pagination": {
                "countPagination": 1,
                "countTotal": 1,
                "nextPage": null
              }
            }
          }
        }
        """;

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      Connection mockConnection = mock(Connection.class);
      Response mockResponse = mock(Response.class);

      jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
      when(mockConnection.method(any())).thenReturn(mockConnection);
      when(mockConnection.header(anyString(), anyString())).thenReturn(mockConnection);
      when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
      when(mockConnection.requestBody(anyString())).thenReturn(mockConnection);
      when(mockConnection.ignoreContentType(true)).thenReturn(mockConnection);
      when(mockConnection.execute()).thenReturn(mockResponse);
      when(mockResponse.body()).thenReturn(json);

      List<EstateResponse.EstateDto> result = webScraper.doScraping();

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Test Estate", result.get(0).headline());
    }
  }

  @Test
  void doScraping_throwsException_whenJsoupFails() {
    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      Connection mockConnection = mock(Connection.class);
      jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
      when(mockConnection.method(any())).thenThrow(new RuntimeException("Network error"));

      assertThrows(RuntimeException.class, () -> webScraper.doScraping());
    }
  }

  @Test
  void extractGrundrissPdfUrl_returnsPdfUrl_whenValidHtmlProvided() throws Exception {
    String htmlWithPdf =
        """
            ...\\"url\\":\\"https://example.com/floorplan.pdf\\",\\"title\\":\\"Grundriss der ME\\"
            """;

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      Connection mockConnection = mock(Connection.class);
      Response mockResponse = mock(Response.class);

      jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
      when(mockConnection.method(any())).thenReturn(mockConnection);
      when(mockConnection.header(anyString(), anyString())).thenReturn(mockConnection);
      when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
      when(mockConnection.ignoreContentType(true)).thenReturn(mockConnection);
      when(mockConnection.execute()).thenReturn(mockResponse);
      when(mockResponse.body()).thenReturn(htmlWithPdf);

      String result = webScraper.extractGrundrissPdfUrl("https://someurl.com");

      assertNotNull(result);
      assertEquals("https://example.com/floorplan.pdf", result);
    }
  }

  @Test
  void extractGrundrissPdfUrl_returnsNull_whenNoPdfFound() throws Exception {
    String htmlWithoutPdf = "<html><body>No floorplan here</body></html>";

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      Connection mockConnection = mock(Connection.class);
      Response mockResponse = mock(Response.class);

      jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
      when(mockConnection.method(any())).thenReturn(mockConnection);
      when(mockConnection.header(anyString(), anyString())).thenReturn(mockConnection);
      when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
      when(mockConnection.ignoreContentType(true)).thenReturn(mockConnection);
      when(mockConnection.execute()).thenReturn(mockResponse);
      when(mockResponse.body()).thenReturn(htmlWithoutPdf);

      String result = webScraper.extractGrundrissPdfUrl("https://someurl.com");

      assertNull(result);
    }
  }
}
