package com.example.stream.name_sink;

import java.util.Date;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameProcessorConfiguration {
  private static final Logger log = LoggerFactory.getLogger(NameProcessorConfiguration.class);
  @Bean
  public Function<String, Person> processName() {

    return name -> {
      log.info("Processing name {}", name);
      final Person person = new Person(name, new Date().getTime());
      log.info("Created person {}", person);
      return person;
    };
  }
}
