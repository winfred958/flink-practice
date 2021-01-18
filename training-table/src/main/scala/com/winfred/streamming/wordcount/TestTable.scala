package com.winfred.streamming.wordcount

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, TableEnvironment}

object TestTable {
  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment
    val tableEnvironment = TableEnvironment
      .create(EnvironmentSettings
        .newInstance()
        .useBlinkPlanner()
        .build())
  }

}
