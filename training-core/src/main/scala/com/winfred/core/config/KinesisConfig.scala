package com.winfred.core.config

import com.winfred.core.config.entity.KinesisConfigEntity
import org.slf4j.{Logger, LoggerFactory}

object KinesisConfig {
  val logger: Logger = LoggerFactory.getLogger(KinesisConfig.getClass)
  val configFilePath: String = "config/kinesis-config.yml"

  def getConfigEntity(): KinesisConfigEntity = {
    val config = new ConfigCommon[KinesisConfigEntity](configFilePath, classOf[KinesisConfigEntity])
    config.getConfigEntity()
  }
}
