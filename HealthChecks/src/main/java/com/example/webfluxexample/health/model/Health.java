package com.example.webfluxexample.health.model;

import java.time.Instant;
import java.util.List;

public record Health(
    Instant timestamp,
    List<ServiceHealth> healthList) {
}
