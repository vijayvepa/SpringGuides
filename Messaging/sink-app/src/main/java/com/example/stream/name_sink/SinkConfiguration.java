package com.example.stream.name_sink;

import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SinkConfiguration {
  @Bean
  public Consumer<Person> nameSink() {
    return person-> {
      System.out.println(person.name());
      System.out.println(person.processedTimestamp());
    };
  }
}
