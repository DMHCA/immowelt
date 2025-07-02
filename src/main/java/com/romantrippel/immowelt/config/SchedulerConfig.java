package com.romantrippel.immowelt.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

  @Bean(destroyMethod = "shutdown")
  public ExecutorService executorService() {
    return Executors.newSingleThreadExecutor();
  }
}
