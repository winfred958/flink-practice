package com.winfred.streamming.cos

import java.time.ZoneId
import java.util.{Properties, UUID}

import com.alibaba.fastjson.JSON
import com.winfred.core.config.KafkaConfig
import com.winfred.core.utils.ArgsHandler
import com.winfred.streamming.entity.log.EventEntity
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.beans.BeanProperty

object CosSinkExample {

  var auto_offset_reset: String = "earliest"
  val sourceTopic: String = "ckafka_test_raw"
  val groupId: String = this.getClass.getCanonicalName

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    import org.apache.flink.streaming.api.scala._

    var basePath = ArgsHandler.getArgsParam(args = args, "target-path")
    if (StringUtils.isBlank(basePath)) {
      basePath = "/tmp/CosExample"
    }
    println(s"output basePath = ${basePath}")

    // source
    val sourceData: DataStream[String] = executionEnvironment
      .addSource(getKafkaSource(topic = sourceTopic, groupId = groupId))


    val result: DataStream[LogEntity] = sourceData
      .map(str => {
        JSON.parseObject(str, classOf[EventEntity])
      })
      .map(entity => {
        LogEntity(
          uuid = entity.getUuid,
          server_time = entity.getServer_time,
          token = entity.getHeader.getToken,
          visitor_id = entity.getHeader.getVisitor_id,
          platform = entity.getHeader.getPlatform
        )
      })

    // sink
    streamingFileSink(
      data = result,
      basePath = basePath,
      bucketCheckInterval = 60000L
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

  def streamingFileSink(
                         data: DataStream[LogEntity],
                         basePath: String,
                         bucketCheckInterval: Long
                       )
  : Unit = {

    val streamingFileSink: StreamingFileSink[LogEntity] = StreamingFileSink
      .forBulkFormat(
        new Path(basePath),
        ParquetAvroWriters.forReflectRecord(classOf[LogEntity])
      )
      .withBucketAssigner(new DateTimeBucketAssigner[LogEntity]("yyyy/MM/dd/HH", ZoneId.of("Asia/Shanghai")))
      .withBucketCheckInterval(bucketCheckInterval)
      .build()

    data
      .addSink(streamingFileSink)
  }

  case class LogEntity(
                        @BeanProperty uuid: String = UUID.randomUUID().toString,
                        @BeanProperty server_time: Long = System.currentTimeMillis(),
                        @BeanProperty token: String = "",
                        @BeanProperty visitor_id: String = "",
                        @BeanProperty platform: String = ""
                      )

}
