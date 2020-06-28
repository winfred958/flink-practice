package com.winfred.core.sink

import java.util.Properties

import com.winfred.core.config.KafkaConfig
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

/**
 * com.winfred.test.rt.sink
 *
 * @author kevin
 * @since 2018/8/29 8:34
 */
object FlinkKafkaSink {

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

  def main(args: Array[String]): Unit = {
    println(classOf[StringSerializer].getCanonicalName)

    val obj = FlinkKafkaSink.getKafkaSink("ttt")
    println(obj)
  }
}
