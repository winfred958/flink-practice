package com.winfred.core.config.entity

import scala.beans.BeanProperty

class KafkaConsumerConfig {
  @BeanProperty var bootstrapServers: String = _
  @BeanProperty var groupId: String = _
  @BeanProperty var enableAutoCommit: Boolean = false
  @BeanProperty var keySerializer: String = _
  @BeanProperty var valueSerializer: String = _
  @BeanProperty var autoOffsetReset: String = _
  @BeanProperty var autoCommitInterval: Long = 1000L
}