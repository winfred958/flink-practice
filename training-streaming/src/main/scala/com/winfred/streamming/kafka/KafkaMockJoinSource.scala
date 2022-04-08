package com.winfred.streamming.kafka

import com.winfred.core.source.JoinDataMockSource
import com.winfred.core.source.entity.OrderJoinMock
import com.winfred.core.utils.ArgsHandler
import com.winfred.streamming.kafka.KafkaMockSource.sinkTopicName
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * mock join 数据
 */
object KafkaMockJoinSource {

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
    val dataStreamSource: DataStream[OrderJoinMock] = executionEnvironment
      .addSource(new JoinDataMockSource(intervalMin, intervalMax))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })

    // MockSourceName
    val orderTopic = "qa_order_test"
    SendKafkaCommon.sinkToOrderTopic(dataStreamSource, orderTopic)

    val orderItemTopic = "qa_order_item_test"
    SendKafkaCommon.sinkToOrderTopic(dataStreamSource, orderItemTopic)

    executionEnvironment.execute(this.getClass.getName)
  }
}
