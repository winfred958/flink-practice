package com.winfred.core.common

import java.util
import java.util.Properties

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.winfred.core.entity.MysqlConfigEntity
import org.slf4j.{Logger, LoggerFactory}
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.beans.BeanProperty
import scala.collection.mutable

object MysqlConfigUtil {

  val LOGGER: Logger = LoggerFactory.getLogger(MysqlConfigUtil.getClass)
  val configFilePath: String = "config/mysql-config.yml"
  val defaultDatasourceName: String = "default"

  /**
   * 默认数据源
   *
   * @return
   */
  def getMysqlConfigEntity(): MysqlConfigEntity = {
    getMysqlConfigEntity(defaultDatasourceName);
  }


  /**
   * 获取指定数据源配置
   *
   * @param dataSourceName
   * @return
   */
  def getMysqlConfigEntity(dataSourceName: String): MysqlConfigEntity = {
    val mysqlConfig = getMysqlConfig()
    if (null == mysqlConfig) {
      return null;
    }
    val map = new mutable.HashMap[String, MysqlConfigEntity]()
    for (my <- mysqlConfig.getMysqlConfig.getDataSource) {
      val mysqlDatabaseHost = my.getHost
      val mysqlDatabasePort = my.getPort
      val mysqlUserName = my.getUserName
      val mysqlPassword = my.getPasswd
      val mysqlDatabaseDriver = my.getDriver
      val mysqlDatabaseName = my.getDatabaseName
      val sparkTruncate = my.getTruncate
      val sparkFetchSize = my.getFetchsize
      val sparkBatchSize = my.getBatchsize
      map
        .put(my.getName, new MysqlConfigEntity(
          mysqlDatabaseHost = mysqlDatabaseHost,
          mysqlDatabasePort = mysqlDatabasePort,
          mysqlDatabaseDriver = mysqlDatabaseDriver,
          mysqlUserName = mysqlUserName,
          mysqlPassword = mysqlPassword,
          mysqlDatabaseName = mysqlDatabaseName,
          sparkTruncate = sparkTruncate,
          sparkFetchSize = sparkFetchSize,
          sparkBatchSize = sparkBatchSize
        ))
    }
    map.get(dataSourceName).get
  }


  /**
   * 获取默认jdbc连接
   *
   * @return
   */
  def getJdbcUrl(): String = {
    val mysqlConfigEntity: MysqlConfigEntity = getMysqlConfigEntity()
    mysqlConfigEntity.getMysqlConnectJdbcUrl
  }

  /**
   * 获取指定jdb链接
   *
   * @param dataSource
   * @return
   */
  def getJdbcUrl(dataSource: String): String = {
    val mysqlConfigEntity: MysqlConfigEntity = getMysqlConfigEntity(dataSource)
    mysqlConfigEntity.getMysqlConnectJdbcUrl
  }

  /**
   * 获取默认数据源,指定database jdbc连接
   *
   * @param dataBase
   * @return
   */
  def getJdbcUrlWithDatabase(dataBase: String): String = {
    val mysqlConfigEntity: MysqlConfigEntity = getMysqlConfigEntity()
    mysqlConfigEntity.setMysqlDatabaseName(dataBase)
    mysqlConfigEntity.getMysqlConnectJdbcUrl
  }


  /**
   * 获取指定数据源,指定database jdbc连接
   *
   * @param dataBase
   * @return
   */
  def getJdbcUrlWithDatabase(dataSource: String, dataBase: String): String = {
    val mysqlConfigEntity: MysqlConfigEntity = getMysqlConfigEntity(dataSource)
    mysqlConfigEntity.setMysqlDatabaseName(dataBase)
    mysqlConfigEntity.getMysqlConnectJdbcUrl
  }

  /**
   * 供使用: customerPriceResultDataFrame.write.mode(SaveMode.Append).jdbc(jdbc_url, table_name_customer_price, properties)
   *
   * @return
   */
  def getMysqlSparkProperties(): Properties = {
    getMysqlSparkProperties(defaultDatasourceName)
  }

  def getMysqlSparkOptions(): util.Map[String, String] = {
    val map: util.Map[String, String] = new util.HashMap()
    val it = getMysqlSparkProperties(defaultDatasourceName).entrySet().iterator()
    while (it.hasNext) {
      val en = it.next()
      map.put(String.valueOf(en.getKey), String.valueOf(en.getValue))
    }
    map
  }

  def getMysqlSparkOptions(dataSourceName: String): util.Map[String, String] = {
    val map: util.Map[String, String] = new util.HashMap()
    val it = getMysqlSparkProperties(dataSourceName).entrySet().iterator()
    while (it.hasNext) {
      val en = it.next()
      map.put(String.valueOf(en.getKey), String.valueOf(en.getValue))
    }
    map
  }

  /**
   *
   * @param dataSourceName 数据源名称
   * @return
   */
  def getMysqlSparkProperties(dataSourceName: String): Properties = {
    val properties: Properties = new Properties()
    val optionMysqlConfigEntity = getMysqlConfigEntity(dataSourceName)
    LOGGER.info("mysql properties config : {}", JSON.toJSONString(optionMysqlConfigEntity, 1, SerializerFeature.SortField))
    properties.setProperty("user", optionMysqlConfigEntity.getMysqlUserName)
    properties.setProperty("password", optionMysqlConfigEntity.getMysqlPassword)
    properties.setProperty("driver", optionMysqlConfigEntity.getMysqlDatabaseDriver)
    properties.setProperty("batchsize", optionMysqlConfigEntity.getSparkBatchSize)
    properties.setProperty("spark.sql.warehouse.dir", "hdfs://xxx/user/hive/warehouse")
    properties.setProperty("truncate", optionMysqlConfigEntity.getSparkTruncate)
    LOGGER.info("mysql spark config : {}", JSON.toJSONString(optionMysqlConfigEntity, 1, SerializerFeature.SortField))
    return properties
  }


  /**
   * 获取yml 配置
   *
   * @return ApplicationContext
   */
  def getMysqlConfig(): MysqlConfig = {
    val inputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(configFilePath)
    try {
      val yaml = new Yaml(new Constructor(classOf[MysqlConfig]))
      return yaml.load(inputStream).asInstanceOf[MysqlConfig]
    } finally {
      if (null != inputStream) {
        inputStream.close()
      }
    }
    return null;
  }

  class MysqlConfig {
    @BeanProperty var mysqlConfig: MyDataSource = _
  }


  class MyDataSource {
    @BeanProperty var dataSource: Array[MyConfig] = _
  }

  class MyConfig {
    @BeanProperty var name: String = _
    @BeanProperty var host: String = _
    @BeanProperty var port: String = _
    @BeanProperty var userName: String = _
    @BeanProperty var passwd: String = _
    @BeanProperty var driver: String = _
    @BeanProperty var databaseName: String = _
    @BeanProperty var truncate: String = _
    @BeanProperty var fetchsize: String = _
    @BeanProperty var batchsize: String = _
  }

  class Warehouse {
    @BeanProperty var dir: String = _
  }

  def main(args: Array[String]): Unit = {
    val mysqlConfigs = MysqlConfigUtil.getMysqlConfig()
    println(mysqlConfigs.getMysqlConfig.getDataSource.apply(0).getHost)
    println(mysqlConfigs.getMysqlConfig.getDataSource.apply(1).getHost)

    println(JSON.toJSONString(MysqlConfigUtil.getMysqlConfigEntity, 1, SerializerFeature.SortField))
    println(JSON.toJSONString(MysqlConfigUtil.getMysqlConfigEntity("xa-test"), 1, SerializerFeature.SortField))
  }
}

