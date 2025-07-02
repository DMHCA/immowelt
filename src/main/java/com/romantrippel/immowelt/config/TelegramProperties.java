package com.romantrippel.immowelt.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

  @NotBlank private String baseUrl;

  @NotBlank private String token;

  @NotBlank private String chatId;
}
