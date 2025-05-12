package com.example.webfluxexample.health.service;

import com.example.webfluxexample.health.checkers.HealthChecker;
import com.example.webfluxexample.health.config.HealthConfig;
import com.example.webfluxexample.health.config.HealthEndpoint;
import com.example.webfluxexample.health.model.Health;
import com.example.webfluxexample.health.model.ServiceHealth;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

@Component
public class HealthMonitor {
  public static Logger log = LoggerFactory.getLogger(HealthMonitor.class);

  private final HealthConfig healthConfig;
  private final HealthChecker topLevelHealthChecker;
  private final ScheduledExecutorService executor;

  private final AtomicReference<FluxSink<Health>> sinkRef = new AtomicReference<>();
  private final AtomicReference<Health> oldHealthReference = new AtomicReference<>();

  public HealthMonitor(
      final HealthConfig healthConfig,
      @Qualifier("top-level-checker") final HealthChecker topLevelHealthChecker) {
    this.healthConfig = healthConfig;
    this.topLevelHealthChecker = topLevelHealthChecker;
    this.executor = Executors.newScheduledThreadPool(1);

    log.info("Received health config {}", this.healthConfig);

    if (!this.healthConfig.isEnabled()) {
      log.info("Health checker is disabled");
      return;
    }

    log.info("Health monitor starting");

    final long initialDelayMillis = healthConfig.getInitialDelayMillis() > 0 ? healthConfig.getInitialDelayMillis() : 10000;
    final long periodMillis = healthConfig.getPeriodMillis() > 0 ? healthConfig.getPeriodMillis() : 20000;

    if (initialDelayMillis != healthConfig.getInitialDelayMillis()
        || periodMillis != healthConfig.getPeriodMillis()) {
      log.warn("Overriding health check rate. initialDelayMillis={}, periodMillis={}", initialDelayMillis, periodMillis);
    }

    this.executor.scheduleWithFixedDelay(this::checkAllHealthEndpoints, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
  }

  private void checkAllHealthEndpoints() {
    try {
      log.debug("Checking health");
      final FluxSink<Health> sink = this.sinkRef.get();
      if (sink == null) {
        log.info("No sink available");
        return;
      }
      final List<ServiceHealth> healthList = this.healthConfig
          .getEndpoints()
          .stream()
          .filter(HealthEndpoint::enabled)
          .map(this::checkHealth)
          .toList();

      final Health lastHealth = this.oldHealthReference.get();

      final Health health = new Health(Instant.now(), healthList);
      final List<ServiceHealth> newHealthList = health.healthList();
      final List<ServiceHealth> oldHealthList = (lastHealth != null) ? lastHealth.healthList() : null;

      // Warn when health changes
      if (oldHealthList != null && !newHealthList.equals(oldHealthList)) {
        log.warn("Health report changed: {}", health);
      }
      this.oldHealthReference.set(health);
      log.debug("Checked health {}", health);
      sink.next(health);
    } catch (final Exception e) {
      log.error("Health check failed", e);
    }
  }

  private ServiceHealth checkHealth(final HealthEndpoint endpoint) {
    return new ServiceHealth(
        endpoint.serviceName(),
        this.topLevelHealthChecker.isHealthy(endpoint));
  }

  public void connect(final FluxSink<Health> sink) {
    log.debug("Connecting health flux");
    this.sinkRef.set(sink);
  }

  public boolean isEnabled() {
    return this.healthConfig.isEnabled();
  }

  public Health getHealthSnapshot() {
    return this.oldHealthReference.get();
  }
}
