package com.romantrippel.immowelt.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.romantrippel.immowelt.entities.EstateEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
class EstateRepositoryTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("testdb")
          .withUsername("testuser")
          .withPassword("testpass");

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private EstateRepository estateRepository;

  private EstateEntity createTestEstate(String key) {
    EstateEntity estate = new EstateEntity();
    estate.setGlobalObjectKey(key);
    estate.setHeadline("Test Headline");
    estate.setEstateType("Apartment");
    estate.setExposeUrl("http://example.com");
    estate.setLivingArea(42.0);
    estate.setImage("img1.jpg");
    estate.setImageHD("img1_hd.jpg");
    estate.setCity("Munich");
    estate.setZip("80331");
    estate.setShowMap(true);
    estate.setStreet("Test Street 123");
    estate.setPriceName("Price");
    estate.setPriceValue("300000");
    estate.setRooms(3);
    estate.setCreatedAt(LocalDateTime.now());
    return estate;
  }

  @Test
  void insertIfNotExists_insertsNewRecord() {
    EstateEntity estate = createTestEstate("unique-key-1");

    int rowsAffected = estateRepository.insertIfNotExists(estate);
    assertThat(rowsAffected).isEqualTo(1);

    Optional<EstateEntity> found = estateRepository.findById("unique-key-1");
    assertThat(found).isPresent();
    assertThat(found.get().getHeadline()).isEqualTo("Test Headline");
  }

  @Test
  void insertIfNotExists_doesNotInsertDuplicate() {
    EstateEntity estate = createTestEstate("unique-key-2");

    int firstInsert = estateRepository.insertIfNotExists(estate);
    int secondInsert = estateRepository.insertIfNotExists(estate);

    assertThat(firstInsert).isEqualTo(1);
    assertThat(secondInsert).isEqualTo(0);

    long count = estateRepository.count();
    assertThat(count).isEqualTo(1);
  }
}
