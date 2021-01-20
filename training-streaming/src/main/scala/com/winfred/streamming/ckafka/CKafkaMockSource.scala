package com.winfred.streamming.ckafka

import com.winfred.core.annotation.PassTest
import com.winfred.core.sink.FlinkKafkaSink
import com.winfred.streamming.common.DataMockSource
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

@PassTest
object CKafkaMockSource {
  var auto_offset_reset: String = "earliest"

  val groupId: String = this.getClass.getCanonicalName
  val sourceTopic: String = "ckafka_test_raw"


  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    import org.apache.flink.streaming.api.scala._

    val dataStream: DataStream[String] = executionEnvironment
      .addSource(new DataMockSource(2, 20))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })
    dataStream
      .addSink(FlinkKafkaSink.getKafkaSink(topic = sourceTopic))
    executionEnvironment
      .execute()
  }
}
