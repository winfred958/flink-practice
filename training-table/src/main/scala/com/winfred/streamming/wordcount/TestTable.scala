package com.winfred.streamming.wordcount

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object TestTable {
  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment
      .getExecutionEnvironment

    val tableEnvironment = StreamTableEnvironment
      .create(executionEnvironment, EnvironmentSettings
        .newInstance()
        .useBlinkPlanner()
        .build())
  }

}
