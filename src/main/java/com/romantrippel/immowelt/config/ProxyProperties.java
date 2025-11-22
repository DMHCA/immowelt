package com.romantrippel.immowelt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {
  private boolean enabled;
  private String host;
  private int port;
  private String username;
  private String password;
}
