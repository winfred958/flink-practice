package com.winfred.training.connector

import com.winfred.core.config.KinesisConfig

object KinesisConfigTest {
  def main(args: Array[String]): Unit = {
    val kinesisConfigEntity = KinesisConfig.getConfigEntity()

    println(kinesisConfigEntity.credentials.basic.accesskeyid)

  }
}
