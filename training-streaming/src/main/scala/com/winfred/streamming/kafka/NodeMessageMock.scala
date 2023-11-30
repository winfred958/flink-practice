package com.winfred.streamming.kafka

import com.winfred.core.source.NoteMessageMockSource
import com.winfred.core.source.entity.NoteMock
import com.winfred.core.utils.ArgsHandler
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object NodeMessageMock {

  var noteSendTopic = "note_send_test"
  var noteReceiptTopic = "note_receipt_test"

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    import org.apache.flink.streaming.api.scala._


    val requestNoteSendTopic = ArgsHandler.getArgsParam(args, "send-topic")
    if (StringUtils.isNotBlank(requestNoteSendTopic)) {
      noteSendTopic = requestNoteSendTopic
    }


    val requestNoteReceiptTopic = ArgsHandler.getArgsParam(args, "receipt-topic")
    if (StringUtils.isNotBlank(requestNoteReceiptTopic)) {
      noteReceiptTopic = requestNoteReceiptTopic
    }

    println(s"=========== note send topic: ${noteSendTopic}")
    println(s"=========== note receipt topic: ${noteReceiptTopic}")

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
    //    dataStreamSource
    //      .map(entity => {
    //        JSON.toJSON(entity)
    //      })
    //      .print()
    // MockSourceName
    SendKafkaCommon.sinkToNoteTopic(dataStreamSource, noteSendTopic)
    SendKafkaCommon.sinkToNoteTopic(dataStreamSource, noteReceiptTopic)

    executionEnvironment.execute(this.getClass.getName)
  }

}
