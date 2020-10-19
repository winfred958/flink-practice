package com.winfred.training.hive

import com.winfred.core.utils.ArgsHandler
import org.apache.flink.table.api.{EnvironmentSettings, Table, TableEnvironment}
import org.apache.flink.table.catalog.hive.HiveCatalog

object FlinkHiveTest {

  def main(args: Array[String]): Unit = {

    val settings: EnvironmentSettings = EnvironmentSettings
      .newInstance()
      .inBatchMode()
      .build()

    val tableEnv = TableEnvironment
      .create(settings)

    var databaseName = ArgsHandler.getArgsParam(args, "database-name")
    if (databaseName == null) {
      databaseName = "default"
    }

    var tableName = ArgsHandler.getArgsParam(args, "table-name")
    if (tableName == null) {
      tableName = "test"
    }

    var hiveConfigDir = ArgsHandler.getArgsParam(args, "hive-config-dir")
    if (hiveConfigDir == null) {
      hiveConfigDir = "/usr/local/service/hive/conf"
    }

    val hiveCatalog = new HiveCatalog(tableName, databaseName, hiveConfigDir)
    tableEnv.registerCatalog(tableName, hiveCatalog)
    tableEnv.useCatalog(tableName)


    val table: Table = tableEnv
      .sqlQuery(
        s"""
           | SELECT * FROM ${tableName} LIMIT 10
           |""".stripMargin)

    val tableResult = table.execute()

    tableResult.print()
  }
}
