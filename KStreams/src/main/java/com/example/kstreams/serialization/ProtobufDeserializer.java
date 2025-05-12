package com.example.kstreams.serialization;

import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtobufDeserializer <T extends GeneratedMessageV3> implements Deserializer<T> {

  private static final Logger log = LoggerFactory.getLogger(ProtobufDeserializer.class);
  /**
   * T of type {@link GeneratedMessageV3} to which data is deserialized.
   */
  private final Class<T> clazzToDecode;

  /**
   * Constructor.
   *
   * @param clazzToDecode expected decoded type
   */
  public ProtobufDeserializer(final Class<T> clazzToDecode) {
    this.clazzToDecode = clazzToDecode;
  }


  @Override
  public T deserialize(final String topic, final byte[] bytes) {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Registry-less deserializer applied for [topic: {}] with class: {}", topic, this.clazzToDecode.getSimpleName());
      }
      if (bytes == null) {
        return null;
      }
      final Method parseFromMethod = this.clazzToDecode.getMethod("parseFrom", byte[].class);
      return (T) parseFromMethod.invoke(null, bytes);
    } catch (final InvocationTargetException e) {
      log.warn("Failed to parse Metric message as InvocationTargetException occurred");
      return null;
    } catch (final ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
