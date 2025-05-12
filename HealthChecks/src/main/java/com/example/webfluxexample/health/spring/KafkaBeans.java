package com.example.webfluxexample.health.spring;

import org.apache.kafka.clients.admin.Admin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaBeans {

  private final KafkaAdmin kafkaAdmin;

  public KafkaBeans(final KafkaAdmin kafkaAdmin) {
    this.kafkaAdmin = kafkaAdmin;
  }

  @Bean
  public Admin admin() {
    return Admin.create(this.kafkaAdmin.getConfigurationProperties());
  }
}
