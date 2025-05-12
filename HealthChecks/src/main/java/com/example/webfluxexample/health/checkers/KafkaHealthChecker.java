package com.example.webfluxexample.health.checkers;

import com.example.webfluxexample.health.config.HealthEndpoint;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.stereotype.Component;

@Component
public class KafkaHealthChecker implements HealthChecker {

  private final Admin admin;

  public KafkaHealthChecker(final Admin admin) {
    this.admin = admin;
  }

  @Override
  public boolean isHealthy(final HealthEndpoint endpoint) {
    final DescribeClusterOptions options = new DescribeClusterOptions()
        .timeoutMs(1000);

    final DescribeClusterResult clusterDescription = this.admin.describeCluster(options);

    // In order to trip health indicator DOWN retrieve data from one of  future objects otherwise indicator is UP even when Kafka is down!!!
    // When Kafka is not connected future.get() throws an exception which in turn sets the indicator DOWN.
    try {
      clusterDescription.clusterId().get();
    } catch (final Exception e) {
      return false;
    }
    return true;
  }
}
