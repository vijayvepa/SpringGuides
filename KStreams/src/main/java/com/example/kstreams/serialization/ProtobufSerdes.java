package com.example.kstreams.serialization;

import com.google.protobuf.GeneratedMessageV3;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtobufSerdes {

  private static Logger log = LoggerFactory.getLogger(ProtobufSerdes.class);
  private static final Map<String, Serde<?>> knownSerdes = new ConcurrentHashMap<>();
  private static final Map<String, ProtobufDeserializer<?>> knownDeserializers = new ConcurrentHashMap<>();

  /**
   * Avoid instantiation.
   */
  private ProtobufSerdes() {
  }

  /**
   * Get {@link Serde} instance bundled with .
   * { @param clazz Class to be used with {@link ProtobufDeserializer}
   *
   * @param <T> denotes the protobuf type.
   * @param clazz clazz
   * @return Serde instance
   * @link com.alertinnovation.awcs.sharedservices.toteinventoryservice.serde.RegistryLessProtobufSerializer } and. {@link ProtobufDeserializer}.
   */
  @SuppressWarnings("unchecked")
  public static <T extends GeneratedMessageV3> Serde<T> forClass(final Class<T> clazz) {
    if (knownSerdes.containsKey(clazz.getName())) {
      if(log.isInfoEnabled()) {
        log.info("Found known serdes for {}", clazz.getName());
      }
      return (Serde<T>) knownSerdes.get(clazz.getName());
    }

    final Serde<T> tSerde = Serdes.serdeFrom(
        new ProtobufSerializer<>(), deserializerFor(clazz)
    );
    knownSerdes.put(clazz.getName(), tSerde);
    return tSerde;
  }

  /**
   * Deserializer for class.
   *
   * @param <T> the type parameter
   * @param clazz the clazz
   * @return the protobuf deserializer
   */
  @SuppressWarnings("unchecked")
  public static <T extends GeneratedMessageV3> ProtobufDeserializer<T> deserializerFor(final Class<T> clazz) {
    if(knownDeserializers.containsKey(clazz.getName())){
      if(log.isInfoEnabled()) {
        log.info("Found known deserializer for {}", clazz.getName());
      }
      return (ProtobufDeserializer<T>) knownDeserializers.get(clazz.getName());
    }
    final ProtobufDeserializer<T> tProtobufDeserializer = new ProtobufDeserializer<>(clazz);
    knownDeserializers.put(clazz.getName(), tProtobufDeserializer);
    return tProtobufDeserializer;
  }

  /**
   * Close all.
   */
  public static void closeAll() {
    log.info("Closing serdes objects for {}", knownSerdes.keySet());

    knownSerdes.values().forEach(Serde::close);
    knownDeserializers.values().forEach(ProtobufDeserializer::close);

    knownDeserializers.clear();
    knownSerdes.clear();
  }
}
