package com.winfred.streamming.kafka

import com.winfred.core.annotation.PassTest
import com.winfred.core.entity.log.EventEntity
import com.winfred.core.sink.FlinkKafkaSink
import com.winfred.core.source.DataMockSource
import com.winfred.core.utils.{ArgsHandler, JsonUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * mock 简单浏览数据
 */
@PassTest
object KafkaMockSource {
  var auto_offset_reset: String = "earliest"

  val groupId: String = this.getClass.getCanonicalName
  val sinkTopicName: String = "kafka_test_raw"


  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    import org.apache.flink.streaming.api.scala._

    var sinkTopic = ArgsHandler.getArgsParam(args, "topic-name")

    var intervalMin = 100L
    val intervalMinStr = ArgsHandler.getArgsParam(args, "interval-min")
    if (!StringUtils.isBlank(intervalMinStr)) {
      intervalMin = intervalMinStr.toLong
    }
    var intervalMax = 500L
    val intervalMaxStr = ArgsHandler.getArgsParam(args, "interval-max")
    if (!StringUtils.isBlank(intervalMaxStr)) {
      intervalMax = intervalMaxStr.toLong
    }

    if (StringUtils.isBlank(sinkTopic)) sinkTopic = sinkTopicName
    val dataStream: DataStream[EventEntity] = executionEnvironment
      .addSource(new DataMockSource(intervalMin, intervalMax))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })

    dataStream
      .map(entity => {
        JsonUtils.toJsonStr(entity)
      })
      .sinkTo(FlinkKafkaSink.getKafkaSink(topic = sinkTopic))

    executionEnvironment.execute(this.getClass.getName)
  }
}
