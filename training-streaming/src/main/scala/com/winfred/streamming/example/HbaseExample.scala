package com.winfred.streamming.example

import com.alibaba.fastjson.JSON
import com.winfred.core.sink.HbaseSink
import com.winfred.core.utils.ArgsHandler
import com.winfred.streamming.common.TestDataMockSource
import com.winfred.streamming.entity.log.EventEntity
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.hadoop.hbase.client.Put

/**
 *    create 'flink_example', {NAME => 'text', VERSIONS => 3}
 */
object HbaseExample {

  private val zookeeperQuorumKey = "zookeeper-quorum"

  private val tableName = "flink_example"

  private val family = "text"
  private val qualifier = "str"


  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    executionEnvironment
      .enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    executionEnvironment.getCheckpointConfig
      .enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION)

    val zookeeperQuorum = ArgsHandler.getArgsParam(args, zookeeperQuorumKey)

    print(s"zookeeperQuorum: ${zookeeperQuorum}")

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
