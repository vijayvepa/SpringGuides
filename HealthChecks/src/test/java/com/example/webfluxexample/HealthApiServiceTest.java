package com.example.webfluxexample;

import com.example.webfluxexample.health.checkers.HealthChecker;
import com.example.webfluxexample.health.config.HealthConfig;
import com.example.webfluxexample.health.config.HealthEndpoint;
import com.example.webfluxexample.health.model.Health;
import com.example.webfluxexample.health.service.HealthApiService;
import com.example.webfluxexample.health.service.HealthMonitor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class HealthApiServiceTest {

  private static Logger log = LoggerFactory.getLogger(HealthApiServiceTest.class);
  private int flux1Counter = 0;
  private int flux2Counter = 0;
  private int flux3Counter = 0;

  private Instant flux1Last = Instant.EPOCH;
  private Instant flux2Last = Instant.EPOCH;
  private Instant flux3Last = Instant.EPOCH;

  /**
   * Test checker always true.
   */
  private static class AlwaysHealthy implements HealthChecker {
    @Override
    public boolean isHealthy(final HealthEndpoint endpoint) {
      return true;
    }
  }

  /**
   * Test checker always false
   */
  private static class AlwaysUnhealthy implements HealthChecker {
    @Override
    public boolean isHealthy(final HealthEndpoint endpoint) {
      return false;
    }
  }

  private HealthConfig generateHealthConfig() {
    final HealthConfig config = new HealthConfig();
    final List<HealthEndpoint> endpoints = new ArrayList<>();
    endpoints.add(new HealthEndpoint(true, "http", "Downstream", "http", "foobar.com", 90, "foo"));
    endpoints.add(new HealthEndpoint(true, "http", "Downstream2", "http", "foobar.com", 91, "foo"));
    config.setEnabled(true);
    config.setEndpoints(endpoints);
    config.setInitialDelayMillis(Duration.ofSeconds(1).toMillis());
    config.setPeriodMillis(Duration.ofSeconds(2).toMillis());
    return config;
  }

  /**
   * This test validates the general hot flux health model.  Each subscription gets only one old health value (if any have been calculated)
   * and then a stream of subsequent updates.  Terminating one subscription should not terminate the underlying hot flux.
   */
  @Test
  void healthCheckTestFluxRestart() throws InterruptedException {
    final HealthConfig config = this.generateHealthConfig();
    final HealthMonitor monitor = new HealthMonitor(config, new AlwaysHealthy());
    final HealthApiService healthApiService = new HealthApiService(monitor);

    final Flux<Health> flux1 = healthApiService.streamHealthUpdates();
    final Flux<Health> flux2 = healthApiService.streamHealthUpdates();

    final Disposable disp1 = flux1.subscribe(item -> {
      log.info("Flux1 received {}", item);
      this.flux1Counter++;
      Assertions.assertTrue(item.timestamp().isAfter(this.flux1Last));
      this.flux1Last = item.timestamp();
    });

    final Disposable disp2 = flux2.subscribe(item -> {
      log.info("Flux2 received {}", item);
      this.flux2Counter++;
      Assertions.assertTrue(item.timestamp().isAfter(this.flux2Last));
      this.flux2Last = item.timestamp();
    });

    Thread.sleep(Duration.ofSeconds(5).toMillis());

    disp1.dispose();
    Thread.sleep(Duration.ofSeconds(5).toMillis());
    disp2.dispose();

    final Flux<Health> flux3 = healthApiService.streamHealthUpdates();

    final Disposable disp3 = flux3.subscribe(item -> {
      log.info("Flux3 received {}", item);
      this.flux3Counter++;
      Assertions.assertTrue(item.timestamp().isAfter(this.flux3Last));
      this.flux3Last = item.timestamp();
    });
    Thread.sleep(Duration.ofSeconds(5).toMillis());

    disp3.dispose();

    Assertions.assertTrue( this.flux1Counter > 0);
    Assertions.assertTrue( this.flux2Counter > 0);
    Assertions.assertTrue( this.flux3Counter > 0);
  }

  @Test
  void healthCheckHealthyPathTest() {
    final HealthConfig config = this.generateHealthConfig();
    final HealthMonitor monitor = new HealthMonitor(config, new AlwaysHealthy());
    final HealthApiService healthApiService = new HealthApiService(monitor);
    final Flux<Health> flux = healthApiService.streamHealthUpdates();

    final Disposable disposable = flux.subscribe(item -> {
      Assertions.assertNotNull(item);
      Assertions.assertTrue(!item.healthList().isEmpty());
      item.healthList().forEach(state -> Assertions.assertTrue(state.healthy()));
    });
    disposable.dispose();
  }

  @Test
  void healthCheckUnhealthyPathTest() {
    final HealthConfig config = this.generateHealthConfig();
    final HealthMonitor monitor = new HealthMonitor(config, new AlwaysUnhealthy());
    final HealthApiService healthApiService = new HealthApiService(monitor);
    final Flux<Health> flux = healthApiService.streamHealthUpdates();

    final Disposable disposable = flux.subscribe(item -> {
      Assertions.assertNotNull(item);
      Assertions.assertTrue(!item.healthList().isEmpty());
      item.healthList().forEach(state -> Assertions.assertFalse(state.healthy()));
    });
    disposable.dispose();
  }

  @Test
  void healthCheckSnapshotTest() {
    final HealthConfig config = this.generateHealthConfig();
    final HealthMonitor monitor = new HealthMonitor(config, new AlwaysHealthy());
    final HealthApiService healthApiService = new HealthApiService(monitor);

    try {
      Thread.sleep(2000);
    } catch (final InterruptedException e) {
      // do nothing
    }
    final Mono<Health> snapshot = healthApiService.getHealthSnapshot();
    final Health health = snapshot.block();
    Assertions.assertNotNull(health);
    Assertions.assertTrue(!health.healthList().isEmpty());
    health.healthList().forEach(state -> Assertions.assertTrue(state.healthy()));
  }
}
