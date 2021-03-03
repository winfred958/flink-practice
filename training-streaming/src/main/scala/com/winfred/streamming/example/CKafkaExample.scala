package com.winfred.streamming.example

import com.winfred.core.annotation.PassTest
import com.winfred.core.sink.FlinkKafkaSink
import com.winfred.core.source.FlinkKafkaSource
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

import java.text.SimpleDateFormat
import java.util.Calendar

@PassTest
object CKafkaExample {

  var auto_offset_reset: String = "earliest"

  val groupId: String = this.getClass.getCanonicalName
  val sourceTopic: String = "ckafka_test_raw"
  val sinkTopic: String = "ckafka_test_target"


  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    executionEnvironment
      .enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    import org.apache.flink.streaming.api.scala._

    // add source
    val dataSource: DataStream[String] = executionEnvironment
      .addSource(
        FlinkKafkaSource.getKafkaSource(
          topics = sourceTopic, groupId = groupId
        )
      )
      .filter(str => {
        StringUtils.isNotBlank(str)
      })

    // data process
    val result: DataStream[String] = dataSource
      .map(str => {
        ("str", 1L)
      })
      .keyBy(0)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
      .reduce((a, b) => {
        (a._1, a._2 + b._2)
      })
      .map(entity => {
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val datetimeStr = dateFormat.format(Calendar.getInstance().getTime)
        s"""{"${datetimeStr}": ${entity._2}}"""
      })

    // add sink
    result
      .addSink(
        FlinkKafkaSink.getKafkaSink(topic = sinkTopic)
      )


    executionEnvironment
      .execute("CKafkaExample")
  }


}
