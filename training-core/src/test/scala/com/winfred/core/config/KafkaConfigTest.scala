package com.winfred.core.config

object KafkaConfigTest {

  def main(args: Array[String]): Unit = {
    val configEntity = KafkaConfig.getConfigEntity()

    println(configEntity)
  }
}
