package com.winfred.iceberg.demo

import cn.hutool.core.bean.BeanUtil
import com.winfred.core.annotation.MockSourceName
import com.winfred.core.source.NoteMessageMockSource
import com.winfred.core.source.entity.ods.NoteSendOds
import com.winfred.core.source.entity.raw.NoteSendRaw
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

import java.time.LocalDate

object IcebergUpsertDemo {

  val catalogName = "hadoop_catalog"
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

    val topicName = "note_send_test"
    val dataStreamSource: DataStream[NoteSendOds] = executionEnvironment
      .addSource(new NoteMessageMockSource(100, 200))
      .filter(entity => {
        val clazz = entity.getClass
        val mockSourceName = clazz.getAnnotation(classOf[MockSourceName])
        val name = mockSourceName.name()
        StringUtils.equals(name, topicName)
      })
      .map(entity => {
        val sendEntity = entity.asInstanceOf[NoteSendRaw]
        val noteSendOds = new NoteSendOds
        BeanUtil.copyProperties(sendEntity, noteSendOds, false)

        noteSendOds
      })
      .name("source-mock")


    tableEnvironment
      .createTemporaryView("input_data_stream_table", dataStreamSource)

    tableEnvironment
      .executeSql(
        s"""
           | CREATE CATALOG ${catalogName}
           | WITH (
           |    'type' = 'iceberg',
           |    'catalog-type' = 'hadoop',
           |    'warehouse' = '${warehousePath}'
           | )
           |""".stripMargin)

    //    --
    tableEnvironment
      .executeSql(
        s"""
           | CREATE TABLE IF NOT EXISTS `${tableName}` (
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
           |    `charge_submit_num` BIGINT,
           |    `request_time`      timestamp,
           |    `send_time`         timestamp,
           |    `full_name`         string,
           |    `campaign_id`       string,
           |    `nodeid`           string,
           |    `process_time`      timestamp,
           |    `dt`                string,
           |    PRIMARY KEY (`dt`, `primary_key`) NOT ENFORCED
           | )
           | PARTITIONED BY (`dt`)
           | WITH (
           |   'connector' = 'iceberg',
           |   'catalog-type'='hadoop',
           |   'catalog-name' = '${catalogName}',
           |   'catalog-database'='${namespaceName}',
           |   'warehouse' = '${warehousePath}',
           |   'format-version' = '2',
           |   'write.wap.enabled' = 'true',
           |   'write.metadata.delete-after-commit.enabled' = 'true',
           |   'write.metadata.previous-versions-max' = '200',
           |   'write.distribution-mode' = 'hash',
           |   'write.upsert.enabled' = 'true'
           | )
           |""".stripMargin)

    tableEnvironment
      .executeSql(
        s"""
           | INSERT INTO `${tableName}`
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
           |   `nodeid`            ,
           |   `process_time`      ,
           |   `dt`
           | FROM
           |   input_data_stream_table
           |""".stripMargin)

    executionEnvironment.execute("flink iceberg upsert")
  }
}
