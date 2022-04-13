package com.winfred.iceberg.demo

import com.winfred.core.source.NoteMessageMockSource
import com.winfred.core.source.entity.NoteMock
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object IcebergUpsertDemo {

  val catalogName = "hadoop_test"
  val namespaceName = "ods"
  val warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"

  val tableName = "note_send_upsert_test"

  def main(args: Array[String]): Unit = {
    val configuration = new Configuration()
    configuration.setBoolean("write.upsert.enabled", true)

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    val tableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(executionEnvironment = executionEnvironment)

    import org.apache.flink.streaming.api.scala._

    val dataStreamSource: DataStream[NoteMock] = executionEnvironment
      .addSource(new NoteMessageMockSource(100, 200))
      .assignAscendingTimestamps(s => {
        System.currentTimeMillis()
      })

    tableEnvironment.createTemporaryView("input_data_stream_table", dataStreamSource)

    // 貌似多余了, 待测试
    tableEnvironment
      .executeSql(
        s"""
           | CREATE CATALOG ${catalogName}
           | WITH (
           |    'type' = 'iceberg'
           |    'catalog-type' = 'hadoop'
           |    'warehouse' = '${warehousePath}'
           | )
           |""".stripMargin)

    tableEnvironment
      .executeSql(
        s"""
           | CREATE TABLE `${catalogName}`.`${namespaceName}`.`${tableName}` (
           |
           | )
           | WITH (
           |   'connector' = 'iceberg'
           |   'catalog-type'='hadoop',
           |   'catalog-name' = '${catalogName}'
           |   'warehouse' = '${warehousePath}'
           | )
           |""".stripMargin)

    tableEnvironment
      .executeSql(
        s"""
           | INSERT INTO `${catalogName}`.`${namespaceName}`.`${tableName}`
           | SELECT
           |   xxx
           | FROM
           |   input_data_stream_table
           |""".stripMargin)

    executionEnvironment.execute("flink iceberg upsert")
  }
}
