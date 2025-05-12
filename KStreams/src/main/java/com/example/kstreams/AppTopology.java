package com.example.kstreams;

import com.example.kstreams.beans.KStreamBeans;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AppTopology {
  private static final Logger log = LoggerFactory.getLogger(AppTopology.class);

  private StreamsBuilder streamsBuilder;

  @Autowired
  public void build(@Qualifier(KStreamBeans.STREAM_BUILDER) final StreamsBuilder streamsBuilder) {
    this.streamsBuilder = streamsBuilder;

    final KStream<String, String> someStream = this.streamsBuilder.stream("some-topic");
    final KStream<String, String> otherStream = this.streamsBuilder.stream("other-topic");

    final KTable<String, String> table = someStream.toTable();
    final KTable<String, String> otherTable = otherStream.toTable();

    table.join(otherTable, (ValueJoiner<String, String, Object>) (s, s2) -> s + ":" + s2).toStream().to("destination");
  }

  /**
   * Prints the Topology post construction.
   */
  @PostConstruct
  public void logTopology() {
    log.info("Topology: \n {}", this.streamsBuilder.build().describe());
  }

}
