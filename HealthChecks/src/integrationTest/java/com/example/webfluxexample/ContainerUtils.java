package com.example.webfluxexample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class ContainerUtils {

  private static Logger log = LoggerFactory.getLogger(ContainerUtils.class);
  /**
   * Create a test kafka container.
   *
   * @return the Kafka container.
   */
  public static KafkaContainer createKafkaContainer() {
    return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
  }
  /**
   * Create a test Mongo container.
   *
   * @return the mongo container.
   */
  public static GenericContainer<?> createMongoContainer() {
    log.info("Creating the mongo DB container");
    return new GenericContainer<>(DockerImageName.parse("mongo:5"))
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "root-pwd")
        .withEnv("MONGO_INITDB_DATABASE", "acs")
        .withClasspathResourceMapping("init-mongo.js",
            "/docker-entrypoint-initdb.d/init-mongo.js",
            BindMode.READ_ONLY)
        .withExposedPorts(27017);
  }
}
