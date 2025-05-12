package com.example.webfluxexample;

import com.google.protobuf.GeneratedMessageV3;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerBeans {

  private static final Logger log = LoggerFactory.getLogger(AbstractServiceIntegrationSetup.class);
  /**
   * Protobuf serialized message producing {@link KafkaTemplate} bean.
   * Only used for external topics.
   *
   * @param kafkaProperties {@link KafkaProperties}.
   * @param <T> protobuf class extends GeneratedMessageV3
   * @return KafkaTemplate bean.
   */
  @Bean
  public <T extends GeneratedMessageV3> KafkaTemplate<String, T> externalTopicPublisher(final KafkaProperties kafkaProperties) {
    return new KafkaTemplate<>(this.protobufProducerFactory(kafkaProperties));
  }

  /**
   * Protobuf producer factory.
   *
   * @param kafkaProperties Spring's (spring.kafka) configuration properties to help bind common properties.
   * @param <T> protobuf class extends GeneratedMessageV3
   * @return Instance of {@link DefaultKafkaProducerFactory}
   */
  @Bean
  public <T extends GeneratedMessageV3> DefaultKafkaProducerFactory<String, T> protobufProducerFactory(final KafkaProperties kafkaProperties) {
    final Map<String, Object> configs = new HashMap<>(kafkaProperties.getProducer().getProperties());
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ProtobufSerializer.class);
    log.info("Producer Configuration for protobuf Kafka Producer: \n {}", configs);
    return new DefaultKafkaProducerFactory<>(configs);
  }

  /**
   * Json producer factory.
   *
   * @param kafkaProperties Spring's (spring.kafka) configuration properties to help bind common properties.
   * @return Instance of {@link DefaultKafkaProducerFactory}
   */
  @Bean
  public DefaultKafkaProducerFactory<String, Object> jsonProducerFactory(final KafkaProperties kafkaProperties) {
    // Set all the producer configs set in application properties under spring.kafka.producer.
    final Map<String, Object> configs = new HashMap<>(kafkaProperties.getProducer().getProperties());
    // The following properties are not to overridden by common KafkaProperties and specific to this producer bean.
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    log.info("Producer Configuration for JSON Kafka Producer: \n {}", configs);
    return new DefaultKafkaProducerFactory<>(configs);
  }

  /**
   * JSON serialized message producing {@link KafkaTemplate} bean.
   * Only used for internal topics.
   *
   * @param kafkaProperties {@link KafkaProperties}.
   * @return KafkaTemplate bean.
   */
  @Bean
  public KafkaTemplate<String, Object> jsonTopicPublisher(final KafkaProperties kafkaProperties) {
    return new KafkaTemplate<>(this.jsonProducerFactory(kafkaProperties));
  }

}
