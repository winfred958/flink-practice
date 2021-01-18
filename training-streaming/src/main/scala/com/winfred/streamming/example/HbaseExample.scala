package com.winfred.streamming.example

import com.winfred.streamming.common.TestDataMockSource
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.hadoop.hbase.client.Put

object HbaseExample {

  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    executionEnvironment
      .enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    executionEnvironment.getCheckpointConfig
      .enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION)

    import org.apache.flink.streaming.api.scala._

    executionEnvironment
      .addSource(new TestDataMockSource(2, 20))
      .map(str => {
        val put = new Put("".getBytes())

      })

    executionEnvironment
      .execute("hbase-test")
  }

}
