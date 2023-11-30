package com.winfred.streamming.example

import com.alibaba.fastjson2.JSON
import com.winfred.core.entity.log.EventEntity
import com.winfred.core.source.FlinkKafkaSource
import com.winfred.core.utils.{ArgsHandler, JsonUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import java.time.ZoneId
import java.util.UUID
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
      .fromSource(FlinkKafkaSource.getKafkaSource(topics = sourceTopic, groupId = groupId), WatermarkStrategy.noWatermarks(), "kafka source")

    val result: DataStream[LogEntity] = sourceData
      .map(str => {
        JsonUtils.parseObject(str, classOf[EventEntity])
      })
      .map(entity => {
        LogEntity(
          uuid = entity.getUuid,
          server_time = entity.getServerTime,
          token = entity.getHeader.getToken,
          visitor_id = entity.getHeader.getVisitorId,
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
