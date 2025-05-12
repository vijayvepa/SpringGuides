package com.example.webfluxexample.health.config;

public record SslConfigProperties(
    String trustStorePath,
    String trustStorePassword,
    String keyAlias,
    String keyStorePassword
) {
}
