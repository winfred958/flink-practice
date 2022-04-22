package com.winfred.streamming.kafka

import com.winfred.core.source.NoteMessageMockSource
import com.winfred.core.source.entity.NoteMock
import com.winfred.core.utils.ArgsHandler
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object NodeMessageMock {

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    import org.apache.flink.streaming.api.scala._

    var noteSendTopic = "note_send_test"

    var noteReceiptTopic = "note_receipt_test"

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

    val dataStreamSource: DataStream[NoteMock] = executionEnvironment
      .addSource(new NoteMessageMockSource(intervalMin, intervalMax))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })

    // MockSourceName

    SendKafkaCommon.sinkToNoteTopic(dataStreamSource, noteSendTopic)


    SendKafkaCommon.sinkToNoteTopic(dataStreamSource, noteReceiptTopic)

    executionEnvironment.execute(this.getClass.getName)
  }

}
