package com.winfred.streamming.kafka

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.winfred.core.annotation.MockSourceName
import com.winfred.core.sink.FlinkKafkaSink
import com.winfred.core.source.JoinDataMockSource
import com.winfred.core.source.entity.OrderJoinMock
import com.winfred.core.utils.ArgsHandler
import com.winfred.streamming.kafka.KafkaMockSource.sinkTopicName
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

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
    val dataStreamSource = executionEnvironment
      .addSource(new JoinDataMockSource(intervalMin, intervalMax))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })

    // MockSourceName
    val orderTopic = "qa_order_test"
    sinkToTopic(dataStreamSource, orderTopic)

    val orderItemTopic = "qa_order_item_test"
    sinkToTopic(dataStreamSource, orderItemTopic)

    executionEnvironment.execute(this.getClass.getName)
  }

  private def sinkToTopic(dataStreamSource: DataStream[OrderJoinMock], orderTopic: String) = {
    import org.apache.flink.streaming.api.scala._
    dataStreamSource
      .filter(entity => {
        val clazz = entity.getClass
        val mockSourceName = clazz.getAnnotation(classOf[MockSourceName])
        val name = mockSourceName.name()
        StringUtils.equals(name, orderTopic)
      })
      .map(entity => {
        JSON.toJSONString(entity, 1, SerializerFeature.SortField)
      })
      .sinkTo(FlinkKafkaSink.getKafkaSink(topic = orderTopic))
      .name(orderTopic)
  }
}
