package com.example.webfluxexample.health.checkers;

import com.example.webfluxexample.health.config.HealthEndpoint;
import com.example.webfluxexample.health.web.SecureWebClient;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@Component
public class HttpHealthChecker  implements HealthChecker {
  private static final Logger log = LoggerFactory.getLogger(HttpHealthChecker.class);
  public static final Duration WEB_CLIENT_DURATION = Duration.ofSeconds(10);

  private static final UriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();

  @Override
  public boolean isHealthy(final HealthEndpoint endpoint) {

    Objects.requireNonNull(endpoint);
    if (endpoint.host() == null || endpoint.host().isEmpty()) {
      throw new RuntimeException("Health endpoint requires host:" +  endpoint);
    }

    String scheme = endpoint.scheme();
    if (scheme == null || scheme.isEmpty()) {
      scheme = "http";
    }

    try {
      final String url = uriBuilderFactory.builder()
          .scheme(scheme)
          .host(endpoint.host())
          .port(endpoint.port())
          .path(endpoint.path())
          .build()
          .toString();

      log.debug("Checking health endpoint: {}", url);

      final WebClient webClient = url.startsWith("https") ? SecureWebClient.getInsecureTrustWebClient(url) : WebClient.create(url);
      final Optional<ResponseEntity<Void>> response = webClient.get().retrieve().toBodilessEntity().blockOptional(WEB_CLIENT_DURATION);
      if (response.isEmpty() || response.get().getStatusCode().isError()) {
        return false;
      }
    } catch (final Exception e) {
      log.debug("Health check failed while checking {}", endpoint.serviceName(), e);
      return false;
    }
    return true;
  }
}
