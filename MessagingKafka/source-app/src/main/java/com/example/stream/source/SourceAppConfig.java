package com.example.stream.source;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SourceAppConfig {
  private final static Logger logger = LoggerFactory.getLogger(SourceAppConfig.class);
  @Bean
  public Supplier<String> supplyName() {
    return () -> {
      logger.info("Sending SomeName");
      return "SomeName";
    };
  }
}
