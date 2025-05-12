package com.example.webfluxexample.health.checkers;

import com.example.webfluxexample.health.config.HealthEndpoint;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoHealthChecker implements HealthChecker {

  private final MongoTemplate mongoTemplate;

  public MongoHealthChecker(final MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public boolean isHealthy(final HealthEndpoint endpoint) {
    try {
      this.mongoTemplate.executeCommand("{ isMaster: 1 }");
    } catch (final Exception exception) {
      return false;
    }
    return true;
  }
}
