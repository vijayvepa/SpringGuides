package com.example.webfluxexample;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;

@TestConfiguration
@DirtiesContext
@ActiveProfiles("it")
public class AbstractServiceIntegrationSetup {
  private final static Logger log = LoggerFactory.getLogger(AbstractServiceIntegrationSetup.class);
  /**
   * Integration testing Kafka container.
   * This container is used for running Kafka broker in an isolated environment for integration testing purposes.
   * The container is created using the `createKafkaContainer` method in the `ContainerUtils` class.
   */
  protected static final KafkaContainer SINGLETON_KAFKA_CONTAINER = ContainerUtils.createKafkaContainer();

  /**
   * Represents a MongoDB container for integration testing purposes.
   * The container is created using the `createMongoContainer` method in the `ContainerUtils` class.
   */
  protected static final GenericContainer<?> SINGLETON_MONGO_CONTAINER = ContainerUtils.createMongoContainer();

  /**
   * This setting helps us manage the container lifecycle in following manner.
   * If this is true, the containers defined here will be started only once per JVM and used across
   * test classes. This is a really efficient and, it will speed up our tests by reducing repetitive container start up times.
   * when this is set to false, the containers will be started per each class (before all) and stopped after the execution (after all).
   */
  private static final boolean USE_CONTAINER_PER_JVM = false;

  static {
    if (USE_CONTAINER_PER_JVM) {
      SINGLETON_KAFKA_CONTAINER.start();
      SINGLETON_MONGO_CONTAINER.start();
      log.info("Started Handler Containers");
      Runtime.getRuntime().addShutdownHook(new Thread(
          () -> {
            SINGLETON_KAFKA_CONTAINER.stop();
            SINGLETON_MONGO_CONTAINER.stop();
            log.info("Stopped Handler Containers");
          }
      ));
    }
  }

  /**
   * Start the Containers.
   */
  @BeforeAll
  public static void startContainer() {
    if (!USE_CONTAINER_PER_JVM) {
      SINGLETON_KAFKA_CONTAINER.start();
      SINGLETON_MONGO_CONTAINER.start();
    }
  }

  /**
   * Stop the Containers.
   */
  @AfterAll
  public static void stopContainer() {
    if (!USE_CONTAINER_PER_JVM) {
      SINGLETON_KAFKA_CONTAINER.stop();
      SINGLETON_MONGO_CONTAINER.stop();
    }
  }

  /**
   * Integration tests that need to add properties with dynamic values to the Environment's set of PropertySources.
   * This method needs to be called in all the subclasses because @TestContainers annotation starts new containers with their own ports for each subclass.
   * and subclasses' spring context needs to be aware of the new ports, won't be, unless we call this method.
   *
   * @param registry dynamic property.
   */
  @DynamicPropertySource
  public static void setupContainerProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", SINGLETON_KAFKA_CONTAINER::getBootstrapServers);
    registry.add("spring.data.mongodb.host", SINGLETON_MONGO_CONTAINER::getHost);
    registry.add("spring.data.mongodb.port", SINGLETON_MONGO_CONTAINER::getFirstMappedPort);
  }

  /**
   * Placeholder for any common before-each logic.
   * NOTE: Keeping it because many sub-classes depend on it.
   */
  @BeforeEach
  public void beforeEachSetup() {
    log.info("Before Each Setup");
  }

  /**
   * Log as mark-down headings.
   *
   * @param format the format
   * @param params the params
   */
  protected static void logMarkdown(final String format, final Object... params) {
    log.info("\n\n" + format, params);
  }
}
