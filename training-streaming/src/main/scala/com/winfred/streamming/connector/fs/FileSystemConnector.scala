package com.winfred.streamming.connector.fs

import java.time.ZoneId

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner
import org.apache.flink.streaming.api.scala.DataStream


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

    val streamingFileSink: StreamingFileSink[LogEntity] = StreamingFileSink
      .forBulkFormat(
        path,
        ParquetAvroWriters.forReflectRecord(classOf[LogEntity])
      )
      .withBucketAssigner(new DateTimeBucketAssigner[LogEntity]("yyyy/MM/dd/HH", ZoneId.of("Asia/Shanghai")))
      .withBucketCheckInterval(bucketCheckInterval)
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
