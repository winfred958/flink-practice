package com.winfred.iceberg.stream

import cn.hutool.core.bean.BeanUtil
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.winfred.core.source.entity.ods.NoteReceiptOds
import com.winfred.core.source.entity.raw.NoteReceiptRaw
import com.winfred.core.utils.ArgsHandler
import com.winfred.iceberg.common.IcebergCommonOption
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.slf4j.{Logger, LoggerFactory}

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NoteReceiptStreamOdsTable {

  val log: Logger = LoggerFactory.getLogger(NoteSendStreamOdsTable.getClass)

  val catalogName = "hadoop_catalog"
  val namespaceName = "ods"
  var warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"

  var checkpointDir: String = "hdfs://spacex-hadoop-qa/flink/checkpoiont"

  val groupId = this.getClass.getName

  var topicNames = "note_receipt_test"
  var tableName = "channel_note_receipt"

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

    val rawDataStream: DataStream[NoteReceiptRaw] = IcebergCommonOption.getRawFromKafka(
      streamEnvironment = streamExecutionEnvironment,
      topicNames = topicNames,
      groupId = groupId
    )
      .map((str: String) => {
        try {
          val objectMapper = new ObjectMapper()
          objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          objectMapper.readValue(str, classOf[NoteReceiptRaw])
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


    val odsStreamSource: DataStream[NoteReceiptOds] = rawDataStream
      .map(raw => {
        val noteReceiptOds = new NoteReceiptOds
        BeanUtil.copyProperties(raw, noteReceiptOds, false)

        var channelReceiveTime = raw.getChannel_receive_time
        if (null == channelReceiveTime) {
          noteReceiptOds.setChannel_receive_time(null)
        } else {
          noteReceiptOds.setChannel_receive_time(channelReceiveTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)))
        }

        var spSendTime = raw.getSp_send_time
        if (null == spSendTime) {
          noteReceiptOds.setSp_send_time(null)
        } else {
          noteReceiptOds.setSp_send_time(spSendTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)))
        }

        var receiveSystemTime = raw.getReceive_system_time
        if (null == receiveSystemTime) {
          noteReceiptOds.setReceive_system_time(null)
        } else {
          noteReceiptOds.setReceive_system_time(receiveSystemTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)))
        }

        // sp send time 分区
        noteReceiptOds.setDt(spSendTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zoneId)))

        noteReceiptOds
      })

    val ods_node_receipt_view = "ods_note_receipt_tmp"
    tableEnvironment
      .createTemporaryView(s"${ods_node_receipt_view}", odsStreamSource)

    // 创建 catalog
    IcebergCommonOption.createHadoopCatalog(tableEnvironment = tableEnvironment, catalogName = catalogName, warehousePath = warehousePath)

    tableEnvironment
      .executeSql(
        s"""
           | CREATE TABLE IF NOT EXISTS `${tableName}` (
           |    `primary_key`               STRING,
           |    `sp_result`                 STRING,
           |    `sp_charge_submit_num`      BIGINT,
           |    `sp_send_time`              STRING,
           |    `channel_receive_time`      STRING,
           |    `receive_system_time`       STRING,
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
           |   'write.distribution-mode' = 'hash',
           |   'write.upsert.enabled' = 'true'
           | )
           |""".stripMargin)

    tableEnvironment
      .executeSql(
        s"""
           | INSERT INTO `${tableName}`
           | SELECT
           |   `primary_key`              ,
           |   `sp_result`                ,
           |   `sp_charge_submit_num`     ,
           |   `sp_send_time`             ,
           |   `channel_receive_time`     ,
           |   `receive_system_time`      ,
           |   `dt`
           | FROM
           |   ${ods_node_receipt_view}
           |""".stripMargin)

//    streamExecutionEnvironment.execute("iceberg note send table")
  }
}
