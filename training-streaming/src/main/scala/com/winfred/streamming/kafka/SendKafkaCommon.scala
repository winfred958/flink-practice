package com.winfred.streamming.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.winfred.core.annotation.MockSourceName
import com.winfred.core.sink.FlinkKafkaSink
import com.winfred.core.source.entity.{NoteMock, OrderJoinMock}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.scala.DataStream

class SendKafkaCommon[T] {

  private val objectMapper = new ObjectMapper()
    .findAndRegisterModules()

  def sinkToTopic(dataStreamSource: DataStream[T], topicName: String) = {
    import org.apache.flink.streaming.api.scala._
    dataStreamSource
      .filter((entity: T) => {
        val clazz = entity.getClass
        val mockSourceName = clazz.getAnnotation(classOf[MockSourceName])
        val name = mockSourceName.name()
        StringUtils.equals(name, topicName)
      })
      .map((entity: T) => {
        objectMapper.writeValueAsString(entity)
      })
      .sinkTo(FlinkKafkaSink.getKafkaSink(topic = topicName))
      .name(topicName)
  }
}

object SendKafkaCommon {

  def sinkToOrderTopic(dataStreamSource: DataStream[OrderJoinMock], topicName: String): Unit = {
    val value = new SendKafkaCommon[OrderJoinMock]()
    value.sinkToTopic(dataStreamSource, topicName)
  }


  def sinkToNoteTopic(dataStreamSource: DataStream[NoteMock], topicName: String): Unit = {
    val value = new SendKafkaCommon[NoteMock]()
    value.sinkToTopic(dataStreamSource, topicName)
  }
}
