package com.winfred.core.source.entity.base;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author winfred
 */
@Slf4j
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser,
                                     DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final String text = jsonParser.getText();
        if (StringUtils.isBlank(text)) {
            return LocalDateTime.now(ZONE_ID);
        }
        Long longValue = System.currentTimeMillis();
        try {
            longValue = Long.valueOf(text);
        } catch (NumberFormatException e) {
            log.error("脏数据", e);
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(longValue), ZONE_ID);
    }
}
