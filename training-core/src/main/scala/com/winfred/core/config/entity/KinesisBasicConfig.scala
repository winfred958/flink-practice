package com.winfred.core.config.entity

import scala.beans.BeanProperty

class KinesisBasicConfig {
  @BeanProperty var accesskeyid: String = _
  @BeanProperty var secretkey: String = _
}
