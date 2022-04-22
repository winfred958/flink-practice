package com.winfred.iceberg.stream

import cn.hutool.core.bean.BeanUtil
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.winfred.core.source.entity.ods.NoteSendOds
import com.winfred.core.source.entity.raw.NoteSendRaw
import com.winfred.core.utils.ArgsHandler
import com.winfred.iceberg.common.IcebergCommonOption
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.slf4j.{Logger, LoggerFactory}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

object NoteSendStreamOdsTable {

  val log: Logger = LoggerFactory.getLogger(NoteSendStreamOdsTable.getClass)

  val catalogName = "hadoop_catalog"
  val namespaceName = "ods"
  var warehousePath: String = "hdfs://spacex-hadoop/iceberg/warehouse"

  var checkpointDir: String = "hdfs://spacex-hadoop/flink/checkpoiont"

  val groupId = this.getClass.getName

  var topicNames = "note_send_test"
  var tableName = "channel_note_send"

  private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

  def main(args: Array[String]): Unit = {

    val requestWarehouse = ArgsHandler.getArgsParam(args, "warehouse-path")
    if (!StringUtils.isBlank(requestWarehouse)) {
      warehousePath = requestWarehouse
    }

    val requestTopics = ArgsHandler.getArgsParam(args, "topic-names")
    if (!StringUtils.isBlank(requestTopics)) {
      topicNames = requestTopics
    }

    val requestTableName = ArgsHandler.getArgsParam(args, "table-name")
    if (!StringUtils.isBlank(requestTableName)) {
      tableName = requestTableName
    }

    val requestCheckpointDir = ArgsHandler.getArgsParam(args, "checkpoiont-dir")
    if (!StringUtils.isBlank(requestCheckpointDir)) {
      checkpointDir = requestCheckpointDir
    }

    val configuration = new Configuration()
    configuration.setBoolean("write.upsert.enabled", true)

    val streamExecutionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamExecutionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    val checkpointConfig = streamExecutionEnvironment.getCheckpointConfig
    checkpointConfig.setCheckpointStorage(s"${checkpointDir}/${tableName}")
    checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    checkpointConfig.setTolerableCheckpointFailureNumber(3)


    val tableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(executionEnvironment = streamExecutionEnvironment)

    import org.apache.flink.streaming.api.scala._

    val rawDataStream: DataStream[NoteSendRaw] = IcebergCommonOption.getRawFromKafka(
      streamEnvironment = streamExecutionEnvironment,
      topicNames = topicNames,
      groupId = groupId)
      .map((str: String) => {
        try {
          val objectMapper = new ObjectMapper()
          objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          objectMapper.readValue(str, classOf[NoteSendRaw])
        } catch {
          case e: Exception => {
            log.error(s"脏数据: ${str}", e)
            null;
          }
        }
      })
      .filter(entity => {
        entity != null
      })

    val odsDataStream: DataStream[NoteSendOds] = rawDataStream
      .map(raw => {
        val noteSendOds = new NoteSendOds
        BeanUtil.copyProperties(raw, noteSendOds, false)
        // FIXME: 处理其他字段转换
        var channelSendTime: LocalDateTime = raw.getChannel_send_time
        if (null == channelSendTime) {
          noteSendOds.setChannel_send_time(null)
        } else {
          noteSendOds.setChannel_send_time(channelSendTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)))
        }

        var businessRequestTime = raw.getBusiness_request_time
        if (null == businessRequestTime) {
          noteSendOds.setBusiness_request_time(null)
        } else {
          noteSendOds.setBusiness_request_time(businessRequestTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)))
        }

        var submitSystemTime = raw.getSubmit_system_time
        if (null == submitSystemTime) {
          noteSendOds.setSubmit_system_time(null)
        } else {
          noteSendOds.setSubmit_system_time(submitSystemTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)))
        }
        // channel_send_time 分区
        noteSendOds.setDt(LocalDateTime.now(zoneId).toLocalDate.format(DateTimeFormatter.ISO_DATE.withZone(zoneId)))
        noteSendOds
      })

    val ods_note_send_view = "ods_note_send_tmp"
    tableEnvironment
      .createTemporaryView(s"${ods_note_send_view}", odsDataStream)

    // 创建 catalog
    IcebergCommonOption.createHadoopCatalog(tableEnvironment = tableEnvironment, catalogName = catalogName, warehousePath = warehousePath)

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
           |    `ext_json`                  STRING,
           |    `business_request_time`     STRING,
           |    `channel_send_time`         STRING,
           |    `submit_system_time`        STRING,
           |    `dt`                        STRING,
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
           |   'write.distribution-mode' = 'none',
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
           |   ${ods_note_send_view}
           |""".stripMargin)

    streamExecutionEnvironment.execute("iceberg note send table")


  }
}
