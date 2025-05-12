package com.example.kstreams.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("streams")
public class KafkaStreamsOverrideConfigProperties {
  public Map<String, String> getOverrides() {
    return overrides;
  }

  public void setOverrides(final Map<String, String> overrides) {
    this.overrides = overrides;
  }

  private Map<String, String> overrides = new HashMap<>();
}
