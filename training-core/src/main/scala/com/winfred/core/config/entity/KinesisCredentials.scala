package com.winfred.core.config.entity

import scala.beans.BeanProperty

class KinesisCredentials {
  @BeanProperty var basic: KinesisBasicConfig = _
}
