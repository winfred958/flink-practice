package com.winfred.streamming.kafka

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.winfred.core.annotation.MockSourceName
import com.winfred.core.sink.FlinkKafkaSink
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.scala.DataStream

object SendKafkaCommon {

  def sinkToTopic(dataStreamSource: DataStream[_], orderTopic: String) = {
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
