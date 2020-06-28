package com.winfred.streamming.connector.fs

import java.time.ZoneId

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.fs.StringWriter
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink

import scala.beans.BeanProperty


object FileSystemConnector {


  /**
   * 官方已经标记过时
   *
   * @param data
   * @param basePath
   */
  @Deprecated
  def bucketFileSink(
                      data: DataStream[Any],
                      basePath: String
                    ): Unit = {
    import org.apache.flink.streaming.api.scala._

    val hdfsSink: BucketingSink[String] = new BucketingSink[String](basePath)

    // FIXME: sink parquet 方案1 : 压缩暂时未实现
    hdfsSink.setBucketer(new DatePartitionBucketer())
    hdfsSink.setInactiveBucketCheckInterval(30 * 1000L)
    hdfsSink.setWriter(new StringWriter[String]("UTF-8"))
    hdfsSink.setInactiveBucketThreshold(30 * 60 * 1000L)
    hdfsSink.setBatchSize(1024 * 1024 * 30)

    data
      .map(entity => {
        JSON.toJSONString(entity, 1, SerializerFeature.SortField)
      })
      .addSink(hdfsSink)
  }

  /**
   * 官方推荐写法
   *
   * https://ci.apache.org/projects/flink/flink-docs-release-1.9/dev/connectors/streamfile_sink.html
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
