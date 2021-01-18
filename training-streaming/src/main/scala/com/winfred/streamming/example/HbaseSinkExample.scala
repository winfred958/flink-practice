package com.winfred.streamming.example

import com.winfred.core.sink.HbaseSink
import com.winfred.streamming.common.TestDataMockSource
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.hadoop.hbase.client.Put

object HbaseSinkExample {

  private val zookeeperQuorum = ""

  private val tableName = ""
  private val family = ""
  private val qualifier = "str"

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    executionEnvironment
      .enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    import org.apache.flink.streaming.api.scala._

    executionEnvironment
      .addSource(new TestDataMockSource(2, 10))
      .map(str => {
        val put: Put = new Put(str.getBytes())
        put.addColumn(
          family.getBytes(),
          qualifier.getBytes(),
          System.currentTimeMillis(),
          str.getBytes()
        )
        put
      })
      .addSink(new HbaseSink(zookeeperQuorum, tableName))

    executionEnvironment
      .execute("hbase-example")
  }

}
