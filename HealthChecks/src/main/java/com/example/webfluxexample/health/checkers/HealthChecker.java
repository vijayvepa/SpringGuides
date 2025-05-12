package com.example.webfluxexample.health.checkers;

import com.example.webfluxexample.health.config.HealthEndpoint;

public interface HealthChecker {
  boolean isHealthy(HealthEndpoint endpoint);
}
