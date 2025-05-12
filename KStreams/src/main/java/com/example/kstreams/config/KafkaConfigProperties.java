package com.example.kstreams.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("kafka")
public class KafkaConfigProperties {

  public String getStateDir() {
    return stateDir;
  }

  public void setStateDir(final String stateDir) {
    this.stateDir = stateDir;
  }

  public String getSchemaRegistry() {
    return schemaRegistry;
  }

  public void setSchemaRegistry(final String schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public String getStreamsApplicationId() {
    return streamsApplicationId;
  }

  public void setStreamsApplicationId(final String streamsApplicationId) {
    this.streamsApplicationId = streamsApplicationId;
  }

  public String getConsumerAutoOffsetReset() {
    return consumerAutoOffsetReset;
  }

  public void setConsumerAutoOffsetReset(final String consumerAutoOffsetReset) {
    this.consumerAutoOffsetReset = consumerAutoOffsetReset;
  }

  public boolean isEnableShutdownClientExceptionHandler() {
    return enableShutdownClientExceptionHandler;
  }

  public void setEnableShutdownClientExceptionHandler(final boolean enableShutdownClientExceptionHandler) {
    this.enableShutdownClientExceptionHandler = enableShutdownClientExceptionHandler;
  }

  public long getKafkaRetryBackoffIntervalMs() {
    return kafkaRetryBackoffIntervalMs;
  }

  public void setKafkaRetryBackoffIntervalMs(final long kafkaRetryBackoffIntervalMs) {
    this.kafkaRetryBackoffIntervalMs = kafkaRetryBackoffIntervalMs;
  }

  /**
   * KStream State Directory.
   */
  private String stateDir;

  /**
   * Schema Registry.
   */
  private String schemaRegistry;

  /**
   * Identifier for the stream processing application.
   */
  private String streamsApplicationId;

  /**
   * What to do when there is no initial offset in Kafka or if the current offset does not exist any more on the server.
   * Can be: earliest,latest or none.
   */
  private String consumerAutoOffsetReset;

  /**
   * Configures the shutownClientExceptionHandler customization.
   */
  private boolean enableShutdownClientExceptionHandler;

  /**
   * The amount of time to wait before attempting to retry a failed request to a given topic partition.
   * This avoids repeatedly sending requests in a tight loop under some failure scenarios.
   */
  private long kafkaRetryBackoffIntervalMs;
}
