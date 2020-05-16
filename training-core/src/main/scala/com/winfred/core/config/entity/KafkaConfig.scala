package com.winfred.core.config.entity

import scala.beans.BeanProperty

class KafkaConfig {
  @BeanProperty var producer: KafkaProducerConfig = _
  @BeanProperty var consumer: KafkaConsumerConfig = _
}