package com.winfred.core.source.entity.raw;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author winfred
 */
public class LocalDateTimeJsonSerializer extends JsonSerializer<Long> {
    
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (null != value) {
            gen.writeNumber(value);
        }
    }
}
