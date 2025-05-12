package com.example.webfluxexample.health.api;

import com.example.webfluxexample.health.config.HealthConfig;
import com.example.webfluxexample.health.model.Health;
import com.example.webfluxexample.health.service.HealthApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/health")
public class HealthRestApi {

  private final HealthApiService healthApiService;
  private final HealthConfig healthConfig;

  public HealthRestApi(
      final HealthApiService healthApiService,
      final HealthConfig healthConfig) {
    this.healthApiService = healthApiService;
    this.healthConfig = healthConfig;
  }

  /**
   * Environment Health updates.
   *
   * @return a stream of updates
   */
  @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<Flux<Health>> getHealthUpdates() {
    if (!this.healthConfig.isEnabled()) {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED.value()).build();
    }
    return ResponseEntity.ok(this.healthApiService.streamHealthUpdates());
  }

  /**
   * Health snapshot.
   *
   * @return single health.
   */
  @GetMapping(value = "/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Mono<Health>> getHealthSnapshot() {
    if (!this.healthConfig.isEnabled()) {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED.value()).build();
    }
    return ResponseEntity.ok(this.healthApiService.getHealthSnapshot());
  }

}
