package com.example.stream.name_sink;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SinkConfiguration {
  private static final Logger log = LoggerFactory.getLogger(SinkConfiguration.class);
  @Bean
  public Consumer<Person> nameSink() {
    return person-> {
      log.info("Received {}", person);
    };
  }
}
