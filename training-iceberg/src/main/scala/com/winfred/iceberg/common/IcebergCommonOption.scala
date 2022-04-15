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

    val rowDataStream: DataStream[String] = streamEnvironment
      .fromSource(FlinkKafkaSource.getKafkaSource(topics = topicNames, groupId = groupId), WatermarkStrategy.noWatermarks(), "note send topic")
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
           | CREATE CATALOG ${catalogName}
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
}
