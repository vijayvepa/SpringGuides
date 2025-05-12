package com.example.webfluxexample.health.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "health-checks")
public class HealthConfig {

  public static final String KAFKA = "Kafka";
  public static final String MONGO = "Mongo";
  public static final String HTTP = "HTTP";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public long getInitialDelayMillis() {
    return initialDelayMillis;
  }

  public void setInitialDelayMillis(final long initialDelayMillis) {
    this.initialDelayMillis = initialDelayMillis;
  }

  public long getPeriodMillis() {
    return periodMillis;
  }

  public void setPeriodMillis(final long periodMillis) {
    this.periodMillis = periodMillis;
  }

  public List<HealthEndpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(final List<HealthEndpoint> endpoints) {
    this.endpoints = endpoints;
  }

  private boolean enabled;
  private long initialDelayMillis;
  private long periodMillis;
  private List<HealthEndpoint> endpoints;
}
