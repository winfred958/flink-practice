package com.winfred.core.config

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException

import scala.beans.BeanProperty

class ConfigCommon[T] {
  @BeanProperty var configFilePath: String = _
  @BeanProperty var clazz: Class[T] = _

  def this(configFilePath: String, clazz: Class[T]) {
    this()
    this.configFilePath = configFilePath
    this.clazz = clazz
  }

  def getConfigEntity(): T = {
    val inputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(configFilePath)
    try {
      val yaml = new Yaml()
      yaml.loadAs(inputStream, clazz)
    } catch {
      case e: YAMLException => {
        throw e
      }
    } finally {
      if (null != inputStream) {
        inputStream.close()
      }
    }
  }
}
