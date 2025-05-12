package com.example.webfluxexample.health.config;

public record HealthEndpoint(
  boolean enabled,
  String type,
  String serviceName,
  String scheme,
  String host,
  int port,
  String path
) {
}
