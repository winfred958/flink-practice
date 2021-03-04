package com.winfred.streamming.table.hive

import com.alibaba.fastjson.JSON
import com.winfred.core.entity.log.EventEntity
import com.winfred.core.source.DataMockSource
import com.winfred.core.utils.ArgsHandler
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.catalog.hive.HiveCatalog

object HiveExample {

  val groupId: String = this.getClass.getCanonicalName
  val sinkTopicName: String = "kafka_test_raw"

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    val settings = EnvironmentSettings
      .newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()

    val tableEnvironment = StreamTableEnvironment
      .create(executionEnvironment, settings)

    val name = "myhive"
    val defaultDatabase = "default"
    val hiveConfDir = "/usr/local/service/hive/conf" // a local path
    val version = "3.1.1"

    val hive = new HiveCatalog(name, defaultDatabase, hiveConfDir, version)
    tableEnvironment.registerCatalog("myhive", hive)

    tableEnvironment.useCatalog("myhive")

    import org.apache.flink.streaming.api.scala._
    var sinkTopic = ArgsHandler.getArgsParam(args, "topic-name")
    if (StringUtils.isBlank(sinkTopic)) sinkTopic = sinkTopicName

    val sourceDs: DataStream[EventEntity] = getEventDataStream(executionEnvironment)

    val sourceTableDs: DataStream[TestTableEntity] = sourceDs
      .map(entity => {
        val server_time = entity.getServer_time
        val header = entity.getHeader
        val action_time = header.getAction_time
        val session_id = header.getSession_id
        val body = entity.getBody
        val event_name = body.getEvent_name
        val event_type = body.getEvent_type
        TestTableEntity(
          server_time = server_time,
          action_time = action_time,
          session_id = session_id,
          event_name = event_name,
          event_type = event_type
        )
      })

    tableEnvironment
      .registerDataStream(
        name = "test_raw", sourceDs
      )

    tableEnvironment


  }

  case class TestTableEntity(
                              server_time: Long,
                              action_time: Long,
                              session_id: String,
                              event_name: String,
                              event_type: String
                            )


  private def getEventDataStream(executionEnvironment: StreamExecutionEnvironment): DataStream[EventEntity] = {
    import org.apache.flink.streaming.api.scala._
    executionEnvironment
      .addSource(new DataMockSource(2, 20))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })
      .filter(str => {
        StringUtils.isNotBlank(str)
      })
      .map(str => {
        JSON.parseObject(str, classOf[EventEntity])
      })
      .filter(entity => {
        entity != null
      })
  }
}
