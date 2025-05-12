package com.example.kstreams.serialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class JsonSerdes {

  private static final Logger log = LoggerFactory.getLogger(JsonSerdes.class);
  private static final Map<String, Serde<?>> knownSerdes = new ConcurrentHashMap<>();
  private static final Map<String, JsonDeserializer<?>> knownDeserializers = new ConcurrentHashMap<>();

  /**
   * Json serde for BotMetric.
   *
   * @param <T> the type parameter
   * @param theClass the t class
   * @return serde serde
   */
  public static <T> Serde<T> forClass(final Class<T> theClass) {

    if(knownSerdes.containsKey(theClass.getName())) {
      if(log.isInfoEnabled()) {
        log.info("Found known serdes for {}", theClass.getName());
      }
      //noinspection unchecked
      return (Serde<T>) knownSerdes.get(theClass.getName());
    }

    final JsonSerializer<T> serializer = new JsonSerializer<>();
    final JsonDeserializer<T> deserializer = deserializerFor(theClass);
    final Serde<T> tSerde = Serdes.serdeFrom(serializer, deserializer);
    knownSerdes.put(theClass.getName(), tSerde);
    return tSerde;
  }

  /**
   * Deserializer for class.
   *
   * @param <T> the type parameter
   * @param theClass the class
   * @return the json deserializer
   */
  @SuppressWarnings("unchecked")
  public static <T> JsonDeserializer<T> deserializerFor(final Class<T> theClass) {
    if(knownDeserializers.containsKey(theClass.getName())) {

      if(log.isInfoEnabled()) {
        log.info("Found known deserializer for {}", theClass.getName());
      }
      return (JsonDeserializer<T>) knownDeserializers.get(theClass.getName());
    }

    final JsonDeserializer<T> tJsonDeserializer = new JsonDeserializer<>(theClass);
    knownDeserializers.put(theClass.getName(), tJsonDeserializer);
    return tJsonDeserializer;
  }

  /**
   * Close all.
   */
  public static void closeAll() {

    log.info("Closing serdes objects for {}", knownSerdes.keySet());
    knownSerdes.values().forEach(Serde::close);
    knownDeserializers.values().forEach(JsonDeserializer::close);

    knownDeserializers.clear();
    knownSerdes.clear();
  }
}
