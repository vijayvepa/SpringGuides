package com.example.kstreams.messaging;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverPartition;
import reactor.kafka.receiver.ReceiverRecord;

public abstract class CommonKafkaClient<T> {

  private static final Logger log = LoggerFactory.getLogger(CommonKafkaClient.class);
  /**
   * The shared Data Flux between multiple consumers.
   */
  private Flux<T> dataFlux;

  /**
   * The topic to consume.
   */
  private final String topicName;

  /**
   * The deserializer to convert the raw bytes into a useful object.
   */
  private final Deserializer<T> deserializer;

  /**
   * General kafka properties.
   */
  private final KafkaProperties kafkaProperties;

  /**
   * Base constructor.
   *
   * @param topicName the topic to consume
   * @param deserializer class to convert the raw bytes into a useful object.
   * @param kafkaProperties general kafka properties.
   */
  protected CommonKafkaClient(final String topicName, final Deserializer<T> deserializer, final KafkaProperties kafkaProperties) {
    this.topicName = topicName;
    this.deserializer = deserializer;
    this.kafkaProperties = kafkaProperties;
  }

  /**
   * Reactive Kafka Consumer requires at least one subscriber to keep the consumption active (cold subscription pattern).
   * This subscription make sures that consumer is always active, even when there are no service level consumers.
   */
  public void initializeFlux() {
    this.dataFlux = this.receiveMessages();
  }

  /**
   * Receive a reactive flux. This consumer does not commit offsets back to Kafka.
   * Each invocation establishes a new subscription to Kafka.
   *
   * @return Flux of T.
   */
  private Flux<T> receiveMessages() {
    final String consumerId = UUID.randomUUID().toString();
    final String groupId = "workload-metrics-" + this.topicName + consumerId;

    final Map<String, Object> consumerProps = Map.of(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaProperties.getBootstrapServers(),
        ConsumerConfig.CLIENT_ID_CONFIG, consumerId,
        ConsumerConfig.GROUP_ID_CONFIG, groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class
    );

    final ReceiverOptions<String, Bytes> receiverOptionsWithSubscription = ReceiverOptions.<String, Bytes>create(consumerProps)
        .subscription(Collections.singleton(this.topicName))
        .addAssignListener(this::seekToLatest)
        .addRevokeListener(partitions -> log.debug("Partitions revoked: {}", partitions));

    return KafkaReceiver.create(receiverOptionsWithSubscription)
        .receive()
        .filter(message -> message.value() != null) // Filter out tombstone records
        .doOnNext(this::logReceivedMessage)
        .map(message -> this.deserializer.deserialize(this.topicName, message.value().get()));
  }

  /**
   * Handles partition assignment.
   *
   * @param partitions the assigned partitions
   */
  private void seekToLatest(final Collection<ReceiverPartition> partitions) {
    log.debug("Partitions assigned: {}", partitions);
    partitions.forEach(partition -> {
      final long endOffset = Optional.ofNullable(partition.endOffset()).orElse(0L);
      // min is offset 0, max is the last but one offset.
      final long seekPosition = Math.max(0L, endOffset - 1);
      log.debug("Seeking partition {} to offset {}", partition.topicPartition(), seekPosition);
      partition.seek(seekPosition);
    });
  }

  /**
   * Logs received messages.
   *
   * @param message the received record
   */
  private void logReceivedMessage(final ReceiverRecord<String, Bytes> message) {
    final ReceiverOffset offset = message.receiverOffset();
    log.debug("Received message: topic-partition={}, offset={}, key={}",
        offset.topicPartition(), offset.offset(), message.key());
  }

  /**
   * Expose the already established hot (actively emitting) flux from Kafka Topic to the consumer.
   *
   * @return Flux of T
   */
  protected Flux<T> accessDataFlux() {
    return this.dataFlux;
  }
}
