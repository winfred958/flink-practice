package com.winfred.core.entity

import scala.beans.BeanProperty

class MysqlConfigEntity extends Serializable {

  private val serialVersionUID = 3569584288717076298L

  @BeanProperty var mysqlDatabaseHost: String = _
  @BeanProperty var mysqlDatabasePort: String = _
  @BeanProperty var mysqlDatabaseDriver: String = _
  var mysqlConnectJdbcUrl: String = _
  @BeanProperty var mysqlUserName: String = _
  @BeanProperty var mysqlPassword: String = _
  @BeanProperty var mysqlDatabaseName: String = _
  @BeanProperty var sparkTruncate: String = _
  @BeanProperty var sparkFetchSize: String = _
  @BeanProperty var sparkBatchSize: String = _


  def this(mysqlDatabaseHost: String, mysqlDatabasePort: String, mysqlDatabaseDriver: String, mysqlUserName: String, mysqlPassword: String, mysqlDatabaseName: String, sparkTruncate: String, sparkFetchSize: String, sparkBatchSize: String) {
    this()
    this.mysqlDatabaseHost = mysqlDatabaseHost
    this.mysqlDatabasePort = mysqlDatabasePort
    this.mysqlDatabaseDriver = mysqlDatabaseDriver
    this.mysqlConnectJdbcUrl = s"jdbc:mysql://${mysqlDatabaseHost}:${mysqlDatabasePort}/${mysqlDatabaseName}?autoReconnect=true&failOverReadOnly=false&tinyInt1isBit=false"
    this.mysqlUserName = mysqlUserName
    this.mysqlPassword = mysqlPassword
    this.mysqlDatabaseName = mysqlDatabaseName
    this.sparkTruncate = sparkTruncate
    this.sparkFetchSize = sparkFetchSize
    this.sparkBatchSize = sparkBatchSize
  }

  def getMysqlConnectJdbcUrl: String = {
    return s"jdbc:mysql://${mysqlDatabaseHost}:${mysqlDatabasePort}/${mysqlDatabaseName}?autoReconnect=true&failOverReadOnly=false&tinyInt1isBit=false"
  }
}



