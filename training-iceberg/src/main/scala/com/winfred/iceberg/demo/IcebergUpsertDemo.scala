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
    IcebergConfigCommon.setDefaultIcebergConfig(configuration)
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
           |    `primary_key`       string,
           |    `user_name`         string,
           |    `shop_key`          string,
           |    `business_type`     string,
           |    `task_id`           string,
           |    `subtask_id`        string,
           |    `content`           string,
           |    `receiver`          string,
           |    `show_id`           string,
           |    `gateway_id`        string,
           |    `gateway_account`   string,
           |    `charge_submit_num` long,
           |    `request_time`      timestamp,
           |    `send_time`         timestamp,
           |    `full_name`         string,
           |    `campaign_id`       string,
           |    `node_id`           string,
           |    `process_time`      timestamp,
           |    `dt`                string
           | )
           | USING iceberg
           | PARTITIONED BY (`dt`)
           | PRIMARY KEY (`dt`, `primary_key`) NOT ENFORCED
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
           |   `primary_key`       ,
           |   `user_name`         ,
           |   `shop_key`          ,
           |   `business_type`     ,
           |   `task_id`           ,
           |   `subtask_id`        ,
           |   `content`           ,
           |   `receiver`          ,
           |   `show_id`           ,
           |   `gateway_id`        ,
           |   `gateway_account`   ,
           |   `charge_submit_num` ,
           |   `request_time`      ,
           |   `send_time`         ,
           |   `full_name`         ,
           |   `campaign_id`       ,
           |   `node_id`           ,
           |   `process_time`      ,
           |   `dt`
           | FROM
           |   input_data_stream_table
           |""".stripMargin)

    executionEnvironment.execute("flink iceberg upsert")
  }
}
