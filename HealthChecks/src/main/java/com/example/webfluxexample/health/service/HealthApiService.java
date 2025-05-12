package com.example.webfluxexample.health.service;

import com.example.webfluxexample.health.model.Health;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class HealthApiService {

  private static final Logger log = LoggerFactory.getLogger(HealthApiService.class);
  private final Flux<Health> healthUpdateFlux;
  private final HealthMonitor healthMonitor;

  /**
   * Constructor.
   *
   * @param monitor monitor
   */
  @Autowired
  public HealthApiService(final HealthMonitor monitor) {
    this.healthMonitor = monitor;
    this.healthUpdateFlux = Flux
        .create(monitor::connect)
        .subscribeOn(Schedulers.boundedElastic())
        .cache(1);  // We don't need the groomer, since we only ever cache 1.
    this.healthUpdateFlux.subscribe(data -> {
      if (log.isDebugEnabled()) {
        log.debug("Health Subscription: {}", data);
      }
    });
  }

  /**
   * Start stream of health Updates.
   *
   * @return Flux stream of health updates.
   */
  public Flux<Health> streamHealthUpdates() {
    log.info("Health Subscription started");
    return this.healthUpdateFlux.share();
  }

  /**
   * Get a single health status update.
   *
   * @return health
   */
  public Mono<Health> getHealthSnapshot() {
    final Health health = this.healthMonitor.getHealthSnapshot();
    return Mono.just(health);
  }
}
