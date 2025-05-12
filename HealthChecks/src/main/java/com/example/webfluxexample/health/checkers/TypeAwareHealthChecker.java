package com.example.webfluxexample.health.checkers;

import com.example.webfluxexample.health.config.HealthEndpoint;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * The type Type aware health checker.
 */
@Component("top-level-checker")
public class TypeAwareHealthChecker implements HealthChecker {
  private final HealthCheckerTypes healthCheckType;

  /**
   * Instantiates a new Type aware health checker.
   *
   * @param healthCheckType the health check type
   */
  public TypeAwareHealthChecker(final HealthCheckerTypes healthCheckType) {
    this.healthCheckType = healthCheckType;
  }

  @Override
  public boolean isHealthy(final HealthEndpoint endpoint) {
    Objects.requireNonNull(endpoint);
    if (!endpoint.enabled()) {
      throw new RuntimeException("Asked to check disabled health endpoint: " + endpoint);
    }
    if (endpoint.serviceName() == null || endpoint.serviceName().isEmpty()) {
      throw new RuntimeException("Health endpoint requires service name: {}" + endpoint);
    }
    return this.healthCheckType.getCheckerForName(endpoint.type()).isHealthy(endpoint);
  }
}
