package com.example.webfluxexample.health.checkers;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckerTypes {

  public HealthCheckerTypes(
      final HttpHealthChecker httpHealthChecker,
      final KafkaHealthChecker kafkaHealthChecker,
      final MongoHealthChecker mongoHealthChecker) {
    this.httpHealthChecker = httpHealthChecker;
    this.kafkaHealthChecker = kafkaHealthChecker;
    this.mongoHealthChecker = mongoHealthChecker;
  }

  /**
   * Types of Health checks.
   */
  enum Type {
    HTTP,
    KAFKA,
    MONGO,
  }

  private final HttpHealthChecker httpHealthChecker;
  private final KafkaHealthChecker kafkaHealthChecker;
  private final MongoHealthChecker mongoHealthChecker;

  private final Map<Type, HealthChecker> healthCheckers = new EnumMap<>(HealthCheckerTypes.Type.class);

  /**
   * Initialize the lookup table of checkers/
   */
  @PostConstruct
  private void init() {
    this.healthCheckers.put(Type.HTTP, this.httpHealthChecker);
    this.healthCheckers.put(Type.KAFKA, this.kafkaHealthChecker);
    this.healthCheckers.put(Type.MONGO, this.mongoHealthChecker);
  }

  /**
   * Get a name return the Type value.
   *
   * @param name the type name
   * @return The type.
   */
  public static HealthCheckerTypes.Type fromName(final String name) {
    if (name == null || name.isEmpty()) {
      return Type.HTTP;
    }
    try {
      return Type.valueOf(name.toUpperCase());
    } catch (final IllegalArgumentException e) {
      throw new RuntimeException("Invalid health check type name: " + name.toUpperCase());
    }
  }

  /**
   * Get checker for a type.
   *
   * @param type the checker type.
   * @return a HealthChecker
   */
  public HealthChecker getCheckerForType(final HealthCheckerTypes.Type type) {
    Objects.requireNonNull(type);
    return this.healthCheckers.get(type);
  }

  /**
   * Get checker for a type name.
   *
   * @param name the checker type name.
   * @return a HealthChecker
   */
  public HealthChecker getCheckerForName(final String name) {
    return this.getCheckerForType(HealthCheckerTypes.fromName(name));
  }
}
