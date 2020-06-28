package com.winfred.core.source;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.util.Properties;

public class KinesisSource<IN> {
  
  private Properties loadConfig() {
    return new Properties();
  }
  
  public SourceFunction<IN> getSinkFunction(String streamName, String region, DeserializationSchema<IN> schema) {
    Properties outputProperties = loadConfig();
    outputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
    outputProperties.setProperty("AggregationEnabled", "false");
    FlinkKinesisConsumer<IN> inFlinkKinesisConsumer = new FlinkKinesisConsumer<IN>(streamName, schema, outputProperties);
    return null;
  }
}
