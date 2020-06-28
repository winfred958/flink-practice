package com.winfred.streamming.connector.fs;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class MinuteSecondPartitionBucketAssigner<IN> implements BucketAssigner<IN, String> {
  
  private static final String DEFAULT_FORMAT_STRING = "yyyy-MM-dd--HH-mm";
  
  private transient DateTimeFormatter dateTimeFormatter;
  
  @Override
  public String getBucketId(IN element, Context context) {
    
    long currentProcessingTime = context.currentProcessingTime();
    if (dateTimeFormatter == null) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_FORMAT_STRING);
    }
    return dateTimeFormatter.format(Instant.ofEpochMilli(currentProcessingTime));
  }
  
  @Override
  public SimpleVersionedSerializer<String> getSerializer() {
    return SimpleVersionedStringSerializer.INSTANCE;
  }
}