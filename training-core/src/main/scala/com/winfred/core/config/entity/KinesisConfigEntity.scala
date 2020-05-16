package com.winfred.core.config.entity

import scala.beans.BeanProperty

class KinesisConfigEntity {
  @BeanProperty var credentials: KinesisCredentials = _
}
