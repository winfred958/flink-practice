package com.winfred.core.config.entity

import scala.beans.BeanProperty

class KafkaProducerConfig {
  @BeanProperty var bootstrapServers: String = _
  @BeanProperty var keySerializer: String = _
  @BeanProperty var valueSerializer: String = _
}