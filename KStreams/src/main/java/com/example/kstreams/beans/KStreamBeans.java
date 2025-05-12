package com.example.kstreams.beans;

import com.example.kstreams.config.KafkaConfigProperties;
import com.example.kstreams.config.KafkaStreamsOverrideConfigProperties;
import com.example.kstreams.serialization.JsonSerdes;
import com.example.kstreams.serialization.ProtobufSerdes;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener;

@Configuration
@EnableKafka
public class KStreamBeans {

  private static final Logger log = LoggerFactory.getLogger(KStreamBeans.class);
  /**
   * Kafka Streams Commit Interval set to 1 seconds as our default (Kafka-Streams default is 30 seconds).
   * The frequency in milliseconds with which to commit processing progress.
   * Decreasing this too much would result in many interim updates at sub-tote level being published to tote.lot.info.
   * Increasing this too much would slow down the sink rate to tote.lot.info since many results would be cached.
   */
  public static final int DEFAULT_KAFKA_STREAMS_PROGRESS_COMMIT_INTERVAL = 5000;

  /**
   * Bean name
   */
  public static final String STREAM_BUILDER = "transformationStreamBuilder";

  /**
   * {@link org.springframework.kafka.config.StreamsBuilderFactoryBean} constructed for
   *
   * @param kafkaConfigProperties {@link KafkaConfigProperties}
   * @param overrideConfigProperties {@link KafkaStreamsOverrideConfigProperties}
   * @param kafkaProperties {@link org.springframework.boot.autoconfigure.kafka.KafkaProperties}
   * @param applicationContext {@link org.springframework.context.ConfigurableApplicationContext}
   * @param meterRegistry Micrometer Meter Registry
   * @return bean instance
   */
  @Bean(name = STREAM_BUILDER)
  public StreamsBuilderFactoryBean streamsBuilderFactory(
      final KafkaConfigProperties kafkaConfigProperties,
      final KafkaStreamsOverrideConfigProperties overrideConfigProperties,
      final KafkaProperties kafkaProperties,
      final ConfigurableApplicationContext applicationContext,
      final MeterRegistry meterRegistry) {
    final Map<String, Object> streamsConfigProperties = new HashMap<>();

    streamsConfigProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaConfigProperties.getStreamsApplicationId());
    streamsConfigProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    streamsConfigProperties.put(StreamsConfig.STATE_DIR_CONFIG, kafkaConfigProperties.getStateDir());

    // LogAndContinueExceptionHandler just logs and continue. (Later think about DLQ Pattern)
    streamsConfigProperties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
        LogAndContinueExceptionHandler.class.getName());

    // Read from the earliest offset
    streamsConfigProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfigProperties.getConsumerAutoOffsetReset());

    streamsConfigProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.BytesSerde.class);
    streamsConfigProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

    streamsConfigProperties.put("schema.registry.url", kafkaConfigProperties.getSchemaRegistry());

    // https://docs.confluent.io/platform/current/streams/developer-guide/memory-mgmt.html
    // Can be overridden by configuration.
    streamsConfigProperties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, DEFAULT_KAFKA_STREAMS_PROGRESS_COMMIT_INTERVAL);

    // Retry back-off. Exponential Back off is not yet available - https://github.com/apache/kafka/pull/14111
    streamsConfigProperties.put(CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG, kafkaConfigProperties.getKafkaRetryBackoffIntervalMs());

    // Optimize the streaming topology.
    streamsConfigProperties.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, StreamsConfig.NO_OPTIMIZATION);

    // override by configuration
    streamsConfigProperties.putAll(overrideConfigProperties.getOverrides());
    log.info("Kafka Streams Configuration: {}", streamsConfigProperties);

    final StreamsBuilderFactoryBean factoryBean = new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(streamsConfigProperties));

    if (kafkaConfigProperties.isEnableShutdownClientExceptionHandler()) {
      factoryBean.setKafkaStreamsCustomizer(kafkaStreams -> {
        kafkaStreams.setStateListener((newState, oldState) -> {
          log.info("KStreams State Change [old: {}] to [new: {}]", oldState, newState);
          if (newState == KafkaStreams.State.ERROR || newState.hasStartedOrFinishedShuttingDown()) {
            log.error("KStreams reached {}. Shutting down Spring Application - Expecting container restart (auto)", newState);
            // Setting exit code as 1, but Spring only emits zero upon application closure.
            // Set restart policy to always for docker engine.
            // In kubernetes, given actuator api moves to unavailable, container will be restarted.
            if (applicationContext.isActive()) {
              log.info("Shutting down application on KStream Error");
              JsonSerdes.closeAll();
              ProtobufSerdes.closeAll();
              SpringApplication.exit(applicationContext, () -> 1);
            }
          }
        });
        // Set a global uncaught exception handler to SHUTDOWN_APPLICATION.
        kafkaStreams.setUncaughtExceptionHandler(exception -> {
          log.error("Exception occurred, attempting to {}", StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT,
              exception);
          return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });
      });

      // Enable micrometer metrics.
      factoryBean.addListener(new KafkaStreamsMicrometerListener(meterRegistry));

    }

    return factoryBean;
  }
}
