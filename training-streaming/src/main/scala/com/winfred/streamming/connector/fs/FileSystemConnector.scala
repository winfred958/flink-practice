package com.winfred.streamming.connector.fs

import org.apache.flink.core.fs.Path
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import org.apache.flink.streaming.api.scala.DataStream

import java.time.ZoneId
import scala.beans.BeanProperty


object FileSystemConnector {

  /**
   * 官方推荐写法
   *
   * https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/connectors/file_sink.html
   *
   * @param data
   * @param basePath
   * @param bucketCheckInterval
   */
  def streamingFileSink(
                         data: DataStream[LogEntity],
                         basePath: String,
                         bucketCheckInterval: Long
                       )
  : Unit = {

    val path = new Path(basePath)

    val policy: DefaultRollingPolicy[LogEntity, String] = DefaultRollingPolicy.builder()
      .withRolloverInterval(60 * 60 * 1000L)
      .withInactivityInterval(60L * 1000L)
      .withMaxPartSize(1024L * 1024L * 128L)
      .build()

    val streamingFileSink: StreamingFileSink[LogEntity] = StreamingFileSink
      .forBulkFormat(
        path,
        ParquetAvroWriters.forReflectRecord(classOf[LogEntity])
      )
      .withBucketAssigner(new DateTimeBucketAssigner[LogEntity]("yyyy/MM/dd/HH", ZoneId.of("Asia/Shanghai")))
      .withBucketCheckInterval(bucketCheckInterval)
      .withRollingPolicy(
        policy
      )
      .build()

    data
      .addSink(streamingFileSink)
  }

  case class LogEntity(
                        @BeanProperty uuid: String = "",
                        @BeanProperty event_name: String = "",
                        @BeanProperty server_time: Long = 0L
                      )

}
