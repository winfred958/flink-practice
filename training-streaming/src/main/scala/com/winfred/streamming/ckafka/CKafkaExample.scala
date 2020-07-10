package com.winfred.streamming.ckafka

import java.util.{Properties, UUID}

import com.winfred.core.config.KafkaConfig
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.beans.BeanProperty

object CKafkaExample {

  var auto_offset_reset: String = "earliest"

  val groupId: String = this.getClass.getCanonicalName
  val sourceTopic: String = "ckafka_test_raw"
  val sinkTopic: String = "ckafka_test_target"

  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    import org.apache.flink.streaming.api.scala._

    // add source
    val dataSource: DataStream[String] = executionEnvironment
      .addSource(
        getKafkaSource(
          topic = sourceTopic, groupId = groupId
        )
      )

    // data process
    val result: DataStream[String] = dataSource
      .filter(str => {
        StringUtils.isNotBlank(str)
      })

    // add sink
    result
      .addSink(
        getKafkaSink(topic = sinkTopic)
      )

    executionEnvironment
      .execute("CKafkaExample")
  }

  def getKafkaSource(topic: String, groupId: String): FlinkKafkaConsumer[String] = {
    val kafkaConfigEntity = KafkaConfig.getConfigEntity()
    val bootstrapServers = kafkaConfigEntity.getKafka.getConsumer.getBootstrapServers

    val properties = new Properties()
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500")
    properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000")
    properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, auto_offset_reset)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)

    val source: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema(), properties)
    source
  }


  def getKafkaSink(topic: String): FlinkKafkaProducer[String] = {
    val kafkaConfigEntity = KafkaConfig.getConfigEntity()
    val bootstrapServers = kafkaConfigEntity.getKafka.getProducer.getBootstrapServers
    val properties = new Properties()
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
    properties.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "900000")
    properties.setProperty(ProducerConfig.ACKS_CONFIG, "0")
    properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "16384")

    val sink: FlinkKafkaProducer[String] = new FlinkKafkaProducer[String](topic, new SimpleStringSchema(), properties)
    sink.setWriteTimestampToKafka(true)
    sink
  }

  case class LogEntity(
                        @BeanProperty uuid: String = UUID.randomUUID().toString,
                        @BeanProperty event_time: Long = System.currentTimeMillis(),
                        @BeanProperty message: String = ""
                      )

}
