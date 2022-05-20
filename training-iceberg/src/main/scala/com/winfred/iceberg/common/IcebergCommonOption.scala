package com.winfred.iceberg.common

import com.winfred.core.source.FlinkKafkaSource
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.TableResult
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

class IcebergCommonOption {

  def getRawFromKafka(streamEnvironment: StreamExecutionEnvironment,
                      topicNames: String, groupId: String): DataStream[String] = {

    import org.apache.flink.streaming.api.scala._

    val kafkaSource = FlinkKafkaSource.getKafkaSource(topics = topicNames, groupId = groupId)
    val rowDataStream: DataStream[String] = streamEnvironment
      .fromSource(kafkaSource, WatermarkStrategy.noWatermarks[String](), "note send topic")
      .filter((str: String) => {
        StringUtils.isNotBlank(str)
      })
    rowDataStream
  }
}

object IcebergCommonOption {


  def createHadoopCatalog(tableEnvironment: StreamTableEnvironment,
                          catalogName: String,
                          warehousePath: String): TableResult = {
    tableEnvironment
      .executeSql(
        s"""
           | CREATE CATALOG IF NOT EXISTS ${catalogName}
           | WITH (
           |    'type' = 'iceberg',
           |    'catalog-type' = 'hadoop',
           |    'warehouse' = '${warehousePath}'
           | )
           |""".stripMargin)
  }

  def getRawFromKafka(streamEnvironment: StreamExecutionEnvironment,
                      topicNames: String, groupId: String): DataStream[String] = {
    new IcebergCommonOption()
      .getRawFromKafka(
        streamEnvironment = streamEnvironment,
        topicNames = topicNames,
        groupId = groupId
      )
  }

  def setTableConfig(tableEnvironment: StreamTableEnvironment,
                     tablePath: String) = {
    tableEnvironment
      .executeSql(
        s"""
           |  ALTER TABLE ${tablePath}
           |  SET (
           |    'write.wap.enabled' = 'true',
           |    'write.target-file-size-bytes' = '536870912',
           |    'write.metadata.delete-after-commit.enabled' = 'true',
           |    'write.metadata.previous-versions-max' = '30',
           |
           |    'format-version' = '2',
           |    'write.upsert.enabled' = 'true',
           |
           |    'commit.manifest-merge.enabled' = 'true',
           |    'history.expire.min-snapshots-to-keep' = '3',
           |    'history.expire.max-snapshot-age-ms' = '3600000',
           |
           |    'write.metadata.metrics.default' = 'truncate(32)'
           |  )
           |""".stripMargin)
  }
}
