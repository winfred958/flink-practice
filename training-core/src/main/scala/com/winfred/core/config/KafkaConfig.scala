package com.winfred.core.config

import com.winfred.core.config.entity.KafkaConfigEntity
import org.slf4j.{Logger, LoggerFactory}

object KafkaConfig {

  val logger: Logger = LoggerFactory.getLogger(KafkaConfig.getClass)
  val configFilePath: String = "config/kafka-config.yml"

  def getConfigEntity(): KafkaConfigEntity = {
    val config = new ConfigCommon[KafkaConfigEntity](configFilePath, classOf[KafkaConfigEntity])
    config.getConfigEntity()
  }
}
