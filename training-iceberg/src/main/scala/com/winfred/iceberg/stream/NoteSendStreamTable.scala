package com.winfred.iceberg.stream

import cn.hutool.core.bean.BeanUtil
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.winfred.core.source.FlinkKafkaSource
import com.winfred.core.source.entity.ods.NoteSendOds
import com.winfred.core.source.entity.raw.NoteSendRaw
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

import java.time.LocalDateTime

object NoteSendStreamTable {

  val catalogName = "hadoop_catalog"
  val namespaceName = "ods"
  val warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"

  val groupId = this.getClass.getName

  var topicName = ""
  var tableName = ""

  def main(args: Array[String]): Unit = {

    val configuration = new Configuration()
    configuration.setBoolean("write.upsert.enabled", true)

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    executionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    val tableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(executionEnvironment = executionEnvironment)

    import org.apache.flink.streaming.api.scala._

    val dataStreamSource: DataStream[NoteSendOds] = executionEnvironment
      .fromSource(FlinkKafkaSource.getKafkaSource(tableName, groupId = groupId), WatermarkStrategy.noWatermarks(), "note send topic")
      .filter(str => {
        StringUtils.isNotBlank(str)
      })
      .map(str => {
        val objectMapper = new ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.readValue(str, classOf[NoteSendRaw])
      })
      .map(raw => {
        val noteSendOds = new NoteSendOds
        BeanUtil.copyProperties(raw, noteSendOds, false)
        // FIXME: 处理其他字段转换
        var datetime: LocalDateTime = noteSendOds.getChannel_send_time
        if (null == datetime) {
          datetime = LocalDateTime.now()
        }
        noteSendOds.setDt(datetime.toLocalDate)
        noteSendOds
      })

    tableEnvironment
      .createTemporaryView("raw_note_send_data", dataStreamSource)

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

    tableEnvironment
      .executeSql(
        s"""
           | CREATE TABLE IF NOT EXISTS `${tableName}` (
           |    `primary_key`               STRING,
           |    `user_name`                 STRING,
           |    `shop_key`                  STRING,
           |    `business_type`             STRING,
           |    `task_id`                   STRING,
           |    `subtask_id`                STRING,
           |    `content`                   STRING,
           |    `receiver`                  STRING,
           |    `show_id`                   STRING,
           |    `gateway_id`                STRING,
           |    `gateway_account`           STRING,
           |    `mobile_type`               STRING,
           |    `charge_submit_num`         BIGINT,
           |    `ext_json`                  MAP<STRING, STRING>,
           |    `business_request_time`     TIMESTAMP(9),
           |    `channel_send_time`         TIMESTAMP(9),
           |    `submit_system_time`        TIMESTAMP(9),
           |    `dt`                        DATE,
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
           |   primary_key                              ,
           |   user_name                                ,
           |   shop_key                                 ,
           |   business_type                            ,
           |   task_id                                  ,
           |   subtask_id                               ,
           |   content                                  ,
           |   receiver                                 ,
           |   show_id                                  ,
           |   gateway_id                               ,
           |   gateway_account                          ,
           |   mobile_type                              ,
           |   charge_submit_num                        ,
           |   ext_json                                 ,
           |   business_request_time                    ,
           |   channel_send_time                        ,
           |   submit_system_time                       ,
           |   dt
           | FROM
           |   raw_note_send_data
           |""".stripMargin)

    executionEnvironment.execute("iceberg note send table")


  }
}
