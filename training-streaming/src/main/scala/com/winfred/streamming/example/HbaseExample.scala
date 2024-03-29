package com.winfred.streamming.example

import com.winfred.core.entity.log.EventEntity
import com.winfred.core.sink.HbaseSink
import com.winfred.core.source.DataMockSource
import com.winfred.core.utils.{ArgsHandler, JsonUtils}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger
import org.apache.hadoop.hbase.client.Put

/**
 * create 'visitor_example', {NAME => 'visitor', VERSIONS => 3}
 */
object HbaseExample {

  private val zookeeperQuorumKey = "zookeeper-quorum"

  private val tableName = "visitor_example"

  private val family = "visitor"
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
      .addSource(new DataMockSource(2, 20))
      .map(entity => {
        entity.getHeader().getVisitorId()
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
