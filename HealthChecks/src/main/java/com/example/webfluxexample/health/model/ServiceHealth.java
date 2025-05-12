package com.example.webfluxexample.health.model;

public record ServiceHealth(
    String serviceName,
    boolean healthy) {
}
