package com.winfred.core.source

import java.util
import java.util.Properties
import java.util.regex.Pattern

import com.winfred.core.config.KafkaConfig
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

/**
 * com.winfred.test.rt.source
 *
 * @author kevin
 * @since 2018/8/29 8:23
 */
object FlinkKafkaSource {

  /**
   * earliest
   * latest
   */
  var auto_offset_reset: String = "earliest"

  /** *
   *
   * @param topics 可以逗号分隔多个topic
   * @param groupId
   * @return
   */
  def getKafkaSource(topics: String, groupId: String): FlinkKafkaConsumer011[String] = {
    val properties = getKafkaSinkProperties()
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    val topicList: util.List[String] = new util.ArrayList[String]()
    for (topic <- topics.split(",")) {
      topicList.add(topic)
    }
    val source: FlinkKafkaConsumer011[String] = new FlinkKafkaConsumer011[String](topicList, new SimpleStringSchema(), properties)
    source
  }


  def getKafkaSource(topics: String, groupId: String, properties: Properties): FlinkKafkaConsumer011[String] = {
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    val topicList: util.List[String] = new util.ArrayList[String]()
    for (topic <- topics.split(",")) {
      topicList.add(topic)
    }
    val source: FlinkKafkaConsumer011[String] = new FlinkKafkaConsumer011[String](topicList, new SimpleStringSchema(), properties)
    source
  }

  /**
   *
   * @param topicRegex topic 正则表达式
   * @param groupId
   * @return
   */
  def getKafkaSourceFromTopicRegex(topicRegex: String, groupId: String): FlinkKafkaConsumer011[String] = {
    val properties = getKafkaSinkProperties()
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    new FlinkKafkaConsumer011[String](Pattern.compile(topicRegex), new SimpleStringSchema(), properties)
  }

  def getKafkaSinkProperties(): Properties = {
    val kafkaConfigEntity = KafkaConfig.getConfigEntity()
    val bootstrapServers = kafkaConfigEntity.getKafka.getConsumer.getBootstrapServers

    val properties = new Properties()
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, auto_offset_reset)
    properties
  }

}
