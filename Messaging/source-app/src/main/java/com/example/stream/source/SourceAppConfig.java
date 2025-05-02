package com.example.stream.source;

import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SourceAppConfig {
  @Bean
  public Supplier<String> supplyName() {
    return () -> "SomeName";
  }
}
