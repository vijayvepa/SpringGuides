package com.example.kstreams.serialization;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  ProtobufSerializer<T extends GeneratedMessageV3> implements Serializer<T> {

  private static final Logger log = LoggerFactory.getLogger(ProtobufSerializer.class);
  @Override
  public byte[] serialize(final String s, final T t) {
    if (t == null) {
      log.warn("Serializing NULL as NULL byte array");
      return null;
    }
    log.info("Protobuf Serialization on [topic: {}], [type: {}] \n Data: \n {}", s, t.getClass(), TextFormat.shortDebugString(t));
    return t.toByteArray();
  }
}