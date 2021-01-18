package com.winfred.streamming.example

import com.alibaba.fastjson.JSON
import com.winfred.core.sink.HbaseSink
import com.winfred.streamming.common.TestDataMockSource
import com.winfred.streamming.entity.log.EventEntity
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.hadoop.hbase.client.Put

object HbaseExample {

  private val zookeeperQuorum = ""

  private val tableName = "example"

  private val family = "text"
  private val qualifier = "str"


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

        val entity = JSON.parseObject(str, classOf[EventEntity])
        val rowKey = entity.getUuid
        val put = new Put(rowKey.getBytes())
          .addColumn(
            family.getBytes(),
            qualifier.getBytes(),
            System.currentTimeMillis(),
            str.getBytes()
          )
        put
      })
      .addSink(new HbaseSink(zookeeperQuorum, tableName))

    executionEnvironment
      .execute("hbase-test")
  }

}
