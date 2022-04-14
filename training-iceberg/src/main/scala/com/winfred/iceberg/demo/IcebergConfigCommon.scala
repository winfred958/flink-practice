package com.winfred.iceberg.demo

import org.apache.flink.configuration.Configuration

object IcebergConfigCommon {


  /**
   * 设置默认参数
   *
   * @param config
   */
  def setDefaultIcebergConfig(config: Configuration) = {

    config.setString("write.wap.enabled", "true")
    config.setInteger("write.metadata.delete-after-commit.enabled", 100)

    config.setString("format-version", "2")
  }

}
