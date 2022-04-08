package com.winfred.streamming.kafka

import com.winfred.core.source.NoteMessageMockSource
import com.winfred.core.utils.ArgsHandler
import com.winfred.streamming.kafka.KafkaMockSource.sinkTopicName
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object NodeMessageMock {

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
    val dataStreamSource = executionEnvironment
      .addSource(new NoteMessageMockSource(intervalMin, intervalMax))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })

    // MockSourceName
    val orderTopic = "note_send_test"
    SendKafkaCommon.sinkToTopic(dataStreamSource, orderTopic)

    val orderItemTopic = "note_receipt_test"
    SendKafkaCommon.sinkToTopic(dataStreamSource, orderItemTopic)

    executionEnvironment.execute(this.getClass.getName)
  }

}
