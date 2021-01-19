package com.winfred.core.source

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime
import com.winfred.core.config.KinesisConfig
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer
import org.apache.flink.streaming.connectors.kinesis.config.{AWSConfigConstants, ConsumerConfigConstants}
import org.apache.flink.streaming.connectors.kinesis.serialization.KinesisDeserializationSchemaWrapper

import java.util
import java.util.Properties

object FlinkKinesisSource {

  def getKinesisSource(streamName: String, region: String): SourceFunction[String] = {
    val kinesisConfig = KinesisConfig.getConfigEntity()
    val accesskeyid = kinesisConfig.getCredentials.getBasic.getAccesskeyid
    val secretkey = kinesisConfig.getCredentials.getBasic.getSecretkey

    val properties = new Properties
    properties.put(AWSConfigConstants.AWS_REGION, region)
    properties.put(AWSConfigConstants.AWS_ACCESS_KEY_ID, accesskeyid)
    properties.put(AWSConfigConstants.AWS_SECRET_ACCESS_KEY, secretkey)
    properties.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST")
    val sourceFunction: SourceFunction[String] = new FlinkKinesisConsumer[String](streamName, new SimpleStringSchema(), properties)
    sourceFunction
  }

  def getKinesisSourceFromRuntime(streamName: String, region: String): SourceFunction[String] = {
    val stringToProperties = KinesisAnalyticsRuntime.getApplicationProperties()
    val properties = stringToProperties.get("ConsumerConfigProperties")
    properties.put(AWSConfigConstants.AWS_REGION, region)
    properties.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST")
    val sourceFunction: SourceFunction[String] = new FlinkKinesisConsumer[String](streamName, new SimpleStringSchema(), properties)
    sourceFunction
  }

  def getKinesisSourceFromRuntime(streams: util.List[String], region: String): SourceFunction[String] = {
    val stringToProperties = KinesisAnalyticsRuntime.getApplicationProperties()
    val properties = stringToProperties.get("ConsumerConfigProperties")
    properties.put(AWSConfigConstants.AWS_REGION, region)
    properties.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST")
    val sourceFunction: SourceFunction[String] = new FlinkKinesisConsumer[String](streams, new KinesisDeserializationSchemaWrapper(new SimpleStringSchema()), properties)
    sourceFunction
  }

}
