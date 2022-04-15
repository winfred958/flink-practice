package com.winfred.iceberg.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.winfred.core.source.FlinkKafkaSource
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.TableResult
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

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

  def getRawFromKafka[T](streamEnvironment: StreamExecutionEnvironment,
                         topicNames: String, groupId: String, clazz: Class[T]): DataStream[T] = {
    import org.apache.flink.streaming.api.scala._

    val rowDataStream: DataStream[T] = streamEnvironment
      .fromSource(FlinkKafkaSource.getKafkaSource(topics = topicNames, groupId = groupId), WatermarkStrategy.noWatermarks(), "note send topic")
      .filter(str => {
        StringUtils.isNotBlank(str)
      })
      .map(str => {
        val objectMapper = new ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.readValue(str, clazz)
      })
    rowDataStream
  }
}
