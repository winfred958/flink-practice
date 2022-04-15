package com.winfred.iceberg.stream

import cn.hutool.core.bean.BeanUtil
import com.winfred.core.source.entity.ods.NoteReceiptOds
import com.winfred.core.source.entity.raw.NoteReceiptRaw
import com.winfred.core.utils.ArgsHandler
import com.winfred.iceberg.common.IcebergCommonOption
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

import java.time.LocalDateTime

object NoteReceiptStreamOdsTable {

  val catalogName = "hadoop_catalog"
  val namespaceName = "ods"
  var warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"

  val groupId = this.getClass.getName

  var topicNames = "note_receipt_test"
  var tableName = "channel_note_receipt"

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

    val configuration = new Configuration()
    configuration.setBoolean("write.upsert.enabled", true)

    val streamEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    val tableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(executionEnvironment = streamEnvironment)

    import org.apache.flink.streaming.api.scala._

    val rawDataStream: DataStream[NoteReceiptRaw] = IcebergCommonOption.getRawFromKafka[NoteReceiptRaw](
      streamEnvironment = streamEnvironment,
      topicNames = topicNames,
      groupId = groupId
    )

    val odsStreamSource: DataStream[NoteReceiptOds] = rawDataStream
      .map(raw => {
        val noteReceiptOds = new NoteReceiptOds
        BeanUtil.copyProperties(raw, noteReceiptOds, false)
        // FIXME: 处理其他字段转换
        var datetime: LocalDateTime = noteReceiptOds.getSp_send_time
        if (null == datetime) {
          datetime = LocalDateTime.now()
        }
        noteReceiptOds.setDt(datetime.toLocalDate)
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
           |    `sp_send_time`              TIMESTAMP(9),
           |    `channel_receive_time`      TIMESTAMP(9),
           |    `receive_system_time`       TIMESTAMP(9),
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
           |   primary_key              ,
           |   sp_result                ,
           |   sp_charge_submit_num     ,
           |   sp_send_time             ,
           |   channel_receive_time     ,
           |   receive_system_time      ,
           |   dt
           | FROM
           |   ${ods_node_receipt_view}
           |""".stripMargin)

    streamEnvironment.execute("iceberg note send table")


  }
}
