package com.winfred.streamming.example

import com.alibaba.fastjson.JSON
import com.winfred.core.sink.HbaseSink
import com.winfred.core.utils.ArgsHandler
import com.winfred.streamming.common.TestDataMockSource
import com.winfred.streamming.entity.log.EventEntity
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger
import org.apache.hadoop.hbase.client.Put

/**
 * create 'visitor_example', {NAME => 'visitor_id', VERSIONS => 3}
 */
object HbaseExample {

  private val zookeeperQuorumKey = "zookeeper-quorum"

  private val tableName = "visitor_example"

  private val family = "visitor_id"
  private val qualifier = "visitor_count"


  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    executionEnvironment
      .enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    executionEnvironment.getCheckpointConfig
      .enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION)

    val zookeeperQuorum = ArgsHandler.getArgsParam(args, zookeeperQuorumKey)

    println(s"zookeeperQuorum: ${zookeeperQuorum}")

    import org.apache.flink.streaming.api.scala._

    executionEnvironment
      .addSource(new TestDataMockSource(2, 20))
      .map(str => {
        val entity = JSON.parseObject(str, classOf[EventEntity])
        entity.getHeader.getVisitor_id
      })
      .map(id => {
        VisitorCount(id, 1L)
      })
      .keyBy(entity => {
        entity.visitor_id
      })
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .trigger(ProcessingTimeTrigger.create())
      .reduce((a, b) => {
        VisitorCount(a.visitor_id, b.count + a.count)
      })
      .map(visitorEntity => {
        val rowKey = visitorEntity.visitor_id
        val put = new Put(rowKey.getBytes())
          .addColumn(
            family.getBytes(),
            qualifier.getBytes(),
            System.currentTimeMillis(),
            String.valueOf(visitorEntity.count).getBytes()
          )
        put
      })
      .addSink(new HbaseSink(zookeeperQuorum, tableName))

    executionEnvironment
      .execute("flink2hbase_example")
  }

  case class VisitorCount(
                           visitor_id: String,
                           count: Long
                         )

}
