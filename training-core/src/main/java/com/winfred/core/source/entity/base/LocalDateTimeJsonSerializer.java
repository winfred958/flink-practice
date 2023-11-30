package com.winfred.core.source.entity.base;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author winfred
 */
public class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {

  @Override
  public void serialize(LocalDateTime localdatetime, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
    if (null != localdatetime) {
      final long epochMilli = localdatetime.toInstant(ZoneOffset.UTC).toEpochMilli();
      gen.writeNumber(epochMilli);
    }
  }
}
