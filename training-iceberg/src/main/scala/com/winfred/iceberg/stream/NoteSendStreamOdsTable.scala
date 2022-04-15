package com.winfred.iceberg.stream

import cn.hutool.core.bean.BeanUtil
import com.winfred.core.source.entity.ods.NoteSendOds
import com.winfred.core.source.entity.raw.NoteSendRaw
import com.winfred.core.utils.ArgsHandler
import com.winfred.iceberg.common.IcebergCommonOption
import com.winfred.iceberg.stream.NoteReceiptStreamOdsTable.topicNames
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

import java.time.LocalDateTime

object NoteSendStreamOdsTable {

  val catalogName = "hadoop_catalog"
  val namespaceName = "ods"
  var warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"

  val groupId = this.getClass.getName

  var topicName = "note_send_test"
  var tableName = "channel_note_send"

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

    val streamExecutionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamExecutionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    val tableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(executionEnvironment = streamExecutionEnvironment)

    import org.apache.flink.streaming.api.scala._

    val rawDataStream: DataStream[NoteSendRaw] = IcebergCommonOption.getRawFromKafka[NoteSendRaw](
      streamEnvironment = streamExecutionEnvironment,
      topicNames = topicName,
      groupId = groupId,
      clazz = classOf[NoteSendRaw]
    )

    val odsDataStream = rawDataStream
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
           |   ${ods_note_send_view}
           |""".stripMargin)

    streamExecutionEnvironment.execute("iceberg note send table")


  }
}
