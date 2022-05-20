package com.winfred.iceberg.trade

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.winfred.core.source.entity.trade.TradeEntity
import com.winfred.core.utils.ArgsHandler
import com.winfred.iceberg.common.IcebergCommonOption
import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.TableResult
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.slf4j.{Logger, LoggerFactory}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

object StandardTradeStream {
  val log: Logger = LoggerFactory.getLogger(StandardTradeStream.getClass)

  val catalogName = "hadoop_catalog"
  var warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"
  var checkpointDir: String = "hdfs://spacex-hadoop-qa/flink/checkpoint"

  val groupId = this.getClass.getName

  var topicNames = "standard_trade_state"

  var namespaceName = "ods"
  var tableName = "std_trade"

  private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)

  def main(args: Array[String]): Unit = {

    warehousePath = ArgsHandler.getArgsParam(args, "warehouse-path", warehousePath)
    checkpointDir = ArgsHandler.getArgsParam(args, "checkpoint-dir", checkpointDir)

    topicNames = ArgsHandler.getArgsParam(args, "topic-names", topicNames)
    namespaceName = ArgsHandler.getArgsParam(args, "namespace-name", namespaceName)
    tableName = ArgsHandler.getArgsParam(args, "table-name", tableName)

    val configuration = new Configuration()
    configuration.setBoolean("write.upsert.enabled", true)

    val streamExecutionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamExecutionEnvironment.configure(configuration)

    streamExecutionEnvironment.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)
    val checkpointConfig = streamExecutionEnvironment.getCheckpointConfig
    checkpointConfig.setCheckpointStorage(s"${checkpointDir}/${tableName}")
    checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    checkpointConfig.setTolerableCheckpointFailureNumber(3)
    checkpointConfig.setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)


    val tableEnvironment: StreamTableEnvironment = StreamTableEnvironment.create(executionEnvironment = streamExecutionEnvironment)

    // 创建 catalog
    IcebergCommonOption.createHadoopCatalog(tableEnvironment, catalogName, warehousePath)

    // 建表, 无需创建 db方式: https://iceberg.apache.org/docs/latest/flink-connector/#table-managed-in-hadoop-catalog
    createTradeTable(tableEnvironment)

    // 修改表属性
    IcebergCommonOption.setTableConfig(tableEnvironment, namespaceName, tableName)

    import org.apache.flink.streaming.api.scala._

    // 获取数据源
    val resultDataStream: DataStream[TradeEntity] = IcebergCommonOption.getRawFromKafka(streamExecutionEnvironment, topicNames, this.getClass.getName)
      .filter(str => {
        StringUtils.isNotBlank(str)
      })
      .map(str => {
        val objectMapper = new ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val tradeEntity = objectMapper.readValue(str, classOf[TradeEntity])

        val created = tradeEntity.getCreated
        if (StringUtils.isBlank(created)) {
          new TradeEntity()
        } else {
          val part = LocalDateTime.parse(created, dateTimeFormatter).format(DateTimeFormatter.ofPattern("yyyy-MM"))

          val deDuplicationKey = getUnionKey(tradeEntity.getUni_order_id, tradeEntity.getUni_shop_id, tradeEntity.getPart)
          tradeEntity.setDe_duplication_key(deDuplicationKey)
          tradeEntity.setPart(part)
          tradeEntity
        }
      })
      .filter(entity => {
        StringUtils.isNotBlank(entity.getDe_duplication_key)
      })
      .filter(entity => {
        StringUtils.isNotBlank(entity.getCreated)
      })

    // 开始写流表
    sinkToIceberg(tableEnvironment, resultDataStream)
      .await()
  }


  private def sinkToIceberg(tableEnvironment: StreamTableEnvironment, resultDataStream: DataStream[TradeEntity]): TableResult = {
    val result_table_view = "ods_std_trade"
    tableEnvironment
      .createTemporaryView(result_table_view, resultDataStream)

    tableEnvironment
      .executeSql(
        s"""
           |  INSERT INTO `${catalogName}`.`${namespaceName}`.`${tableName}`
           |  SELECT
           |    `de_duplication_key`    ,
           |    `uni_order_id`          ,
           |    `data_from`             ,
           |    `partner`               ,
           |    `plat_code`             ,
           |    `order_id`              ,
           |    `uni_shop_id`           ,
           |    `uni_id`                ,
           |    `guide_id`              ,
           |    `shop_id`               ,
           |    `plat_account`          ,
           |    `total_fee`             ,
           |    `item_discount_fee`     ,
           |    `trade_discount_fee`    ,
           |    `adjust_fee`            ,
           |    `post_fee`              ,
           |    `discount_rate`         ,
           |    `payment_no_postfee`    ,
           |    `payment`               ,
           |    `pay_time`              ,
           |    `product_num`           ,
           |    `order_status`          ,
           |    `is_refund`             ,
           |    `refund_fee`            ,
           |    `insert_time`           ,
           |    `created`               ,
           |    `endtime`               ,
           |    `modified`              ,
           |    `trade_type`            ,
           |    `receiver_name`         ,
           |    `receiver_country`      ,
           |    `receiver_state`        ,
           |    `receiver_city`         ,
           |    `receiver_district`     ,
           |    `receiver_town`         ,
           |    `receiver_address`      ,
           |    `receiver_mobile`       ,
           |    `trade_source`          ,
           |    `delivery_type`         ,
           |    `consign_time`          ,
           |    `orders_num`            ,
           |    `is_presale`            ,
           |    `presale_status`        ,
           |    `first_fee_paytime`     ,
           |    `last_fee_paytime`      ,
           |    `first_paid_fee`        ,
           |    `tenant`                ,
           |    `tidb_modified`         ,
           |    `step_paid_fee`         ,
           |    `seller_flag`           ,
           |    `is_used_store_card`    ,
           |    `store_card_used`       ,
           |    `store_card_basic_used` ,
           |    `store_card_expand_used`,
           |    `order_promotion_num`   ,
           |    `item_promotion_num`    ,
           |    `buyer_remark`          ,
           |    `seller_remark`         ,
           |    `trade_business_type`   ,
           |    `part`
           |  FROM
           |    `${result_table_view}`
           |""".stripMargin)
  }

  private def getUnionKey(uniOrderId: String, uniShopId: String, partner: String): String = {
    s"${uniOrderId}|${uniShopId}|${partner}"
  }

  private def createTradeTable(tableEnvironment: StreamTableEnvironment): TableResult = {
    tableEnvironment
      .executeSql(s"CREATE TABLE IF NOT EXISTS ${namespaceName}")

    tableEnvironment
      .executeSql(s"USE ${namespaceName}")

    val sql =
      s"""
         |  CREATE TABLE IF NOT EXISTS `${catalogName}`.`${namespaceName}`.`${tableName}`
         |  (
         |    `de_duplication_key`     string,
         |    `uni_order_id`           string,
         |    `data_from`              bigint,
         |    `partner`                string,
         |    `plat_code`              string,
         |    `order_id`               string,
         |    `uni_shop_id`            string,
         |    `uni_id`                 string,
         |    `guide_id`               string,
         |    `shop_id`                string,
         |    `plat_account`           string,
         |    `total_fee`              double,
         |    `item_discount_fee`      double,
         |    `trade_discount_fee`     double,
         |    `adjust_fee`             double,
         |    `post_fee`               double,
         |    `discount_rate`          double,
         |    `payment_no_postfee`     double,
         |    `payment`                double,
         |    `pay_time`               string,
         |    `product_num`            bigint,
         |    `order_status`           string,
         |    `is_refund`              string,
         |    `refund_fee`             double,
         |    `insert_time`            string,
         |    `created`                string,
         |    `endtime`                string,
         |    `modified`               string,
         |    `trade_type`             string,
         |    `receiver_name`          string,
         |    `receiver_country`       string,
         |    `receiver_state`         string,
         |    `receiver_city`          string,
         |    `receiver_district`      string,
         |    `receiver_town`          string,
         |    `receiver_address`       string,
         |    `receiver_mobile`        string,
         |    `trade_source`           string,
         |    `delivery_type`          string,
         |    `consign_time`           string,
         |    `orders_num`             bigint,
         |    `is_presale`             bigint,
         |    `presale_status`         string,
         |    `first_fee_paytime`      string,
         |    `last_fee_paytime`       string,
         |    `first_paid_fee`         double,
         |    `tenant`                 string,
         |    `tidb_modified`          string,
         |    `step_paid_fee`          double,
         |    `seller_flag`            string,
         |    `is_used_store_card`     bigint,
         |    `store_card_used`        double,
         |    `store_card_basic_used`  double,
         |    `store_card_expand_used` double,
         |    `order_promotion_num`    bigint,
         |    `item_promotion_num`     bigint,
         |    `buyer_remark`           string,
         |    `seller_remark`          string,
         |    `trade_business_type`    string,
         |    `part`                   string,
         |    PRIMARY KEY (`part`, `de_duplication_key`) NOT ENFORCED
         |  )
         |  PARTITIONED BY (`part`)
         |  WITH (
         |    'connector' = 'iceberg',
         |    'catalog-type'='hadoop',
         |    'catalog-name' = '${catalogName}',
         |    'catalog-database'='${namespaceName}',
         |    'warehouse' = '${warehousePath}',
         |
         |    'write.upsert.enabled' = 'true',
         |
         |    'write.wap.enabled' = 'true',
         |    'write.target-file-size-bytes' = '536870912',
         |    'write.metadata.delete-after-commit.enabled' = 'true',
         |    'write.metadata.previous-versions-max' = '100',
         |    'write.metadata.compression-codec' = 'gzip',
         |
         |    'format-version' = '2',
         |
         |    'commit.manifest-merge.enabled' = 'true',
         |    'history.expire.min-snapshots-to-keep' = '3',
         |    'history.expire.max-snapshot-age-ms' = '10800000'
         |  )
         |""".stripMargin

    tableEnvironment.executeSql(sql)
  }
}

