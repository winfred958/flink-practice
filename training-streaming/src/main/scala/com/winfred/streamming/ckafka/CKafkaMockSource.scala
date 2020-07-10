package com.winfred.streamming.ckafka

import com.winfred.core.sink.FlinkKafkaSink
import com.winfred.streamming.ckafka.CKafkaExample.sinkTopic
import com.winfred.streamming.common.TestSource
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object CKafkaMockSource {
  var auto_offset_reset: String = "earliest"

  val groupId: String = this.getClass.getCanonicalName
  val sourceTopic: String = "ckafka_test_raw"


  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    import org.apache.flink.streaming.api.scala._


    val datastream = executionEnvironment
      .addSource(new TestSource(200));

    datastream
      .addSink(FlinkKafkaSink.getKafkaSink(topic = sinkTopic))


    executionEnvironment
      .execute("CKafkaTestSource")
  }

}
