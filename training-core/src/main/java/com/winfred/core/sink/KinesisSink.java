package com.winfred.core.sink;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisProducer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.util.Properties;

public class KinesisSink<OUT> {

    private Properties loadConfig() {
        return new Properties();
    }

    public SinkFunction<OUT> getSinkFunction(String streamName, String region, SerializationSchema<OUT> schema) {
        Properties outputProperties = loadConfig();
        outputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
        outputProperties.setProperty("AggregationEnabled", "false");
        FlinkKinesisProducer<OUT> outFlinkKinesisProducer = new FlinkKinesisProducer<>(schema, outputProperties);
        outFlinkKinesisProducer.setDefaultStream(streamName);
        return outFlinkKinesisProducer;
    }
}
