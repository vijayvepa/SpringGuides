package com.example.kstreams.messaging;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import reactor.core.publisher.Flux;

//@Component
public class StringKafkaClient extends CommonKafkaClient<String>{

  private static final Logger log = LoggerFactory.getLogger(StringKafkaClient.class);
  /**
   * Base constructor.
   *
   * @param topicName the topic to consume
   * @param deserializer class to convert the raw bytes into a useful object.
   * @param kafkaProperties general kafka properties.
   */
  protected StringKafkaClient(final String topicName, final StringDeserializer deserializer,
      final KafkaProperties kafkaProperties) {
    super(topicName, deserializer, kafkaProperties);
  }


  @PostConstruct
  private void publishText() {

    final Flux<String> stringFlux = this.accessDataFlux();

    stringFlux
        .subscribe(
            item -> {
              if (log.isInfoEnabled()) {
                log.info("Default Subscription: Item: {}", item);
              }
              if (item != null) {
                if (log.isInfoEnabled()) {
                  log.info("Received Item: {}", item);
                }
              }
            }
        );
  }
}
