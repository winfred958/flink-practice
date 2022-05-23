package com.winfred.iceberg.trade

import com.alibaba.fastjson.JSON
import com.winfred.core.source.entity.trade.OrderEntity
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

object StandardOrderStream {
  val log: Logger = LoggerFactory.getLogger(StandardTradeStream.getClass)

  val catalogName = "hadoop_catalog"
  var warehousePath: String = "hdfs://spacex-hadoop-qa/iceberg/warehouse"
  var checkpointDir: String = "hdfs://spacex-hadoop-qa/flink/checkpoint"

  val groupId = this.getClass.getName

  var topicNames = "standard_trade_order"

  var namespaceName = "ods"
  var tableName = "std_order"

  private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)
  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM").withZone(zoneId)

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

    // 建表, 无需创建 db方式: https://iceberg.apache.org/docs/latest/flink-connector/#table-managed-in-hadoop-catalog
    createTradeTable(tableEnvironment)

    // 修改表属性
    IcebergCommonOption.setTableConfig(tableEnvironment, tableName)

    import org.apache.flink.streaming.api.scala._

    // 获取子订单数据
    val resultDataStream: DataStream[OrderEntity] = IcebergCommonOption.getRawFromKafka(streamExecutionEnvironment, topicNames, this.getClass.getName)
      .filter(str => {
        StringUtils.isNotBlank(str)
      })
      .map(str => {
        //        val objectMapper = new ObjectMapper()
        //        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        //        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val orderEntity = JSON.parseObject(str, classOf[OrderEntity])

        val created = orderEntity.getCreated
        if (StringUtils.isBlank(created)) {
          new OrderEntity()
        } else {
          val part = LocalDateTime.parse(created, dateTimeFormatter).format(dateFormatter)

          val deDuplicationKey = getUnionKey(orderEntity.getUni_order_item_id, orderEntity.getTenant, orderEntity.getPart)
          orderEntity.setDe_duplication_key(deDuplicationKey)
          orderEntity.setPart(part)
          orderEntity
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

    streamExecutionEnvironment.execute("StandardOrderStream")
  }


  private def sinkToIceberg(tableEnvironment: StreamTableEnvironment, resultDataStream: DataStream[OrderEntity]): TableResult = {
    val result_table_view = "ods_std_order"
    tableEnvironment
      .createTemporaryView(result_table_view, resultDataStream)

    tableEnvironment
      .executeSql(
        s"""
           |  INSERT INTO `${tableName}`
           |  SELECT
           |    de_duplication_key     ,
           |    uni_order_item_id      ,
           |    data_from              ,
           |    partner                ,
           |    uni_order_id           ,
           |    plat_code              ,
           |    order_item_id          ,
           |    order_id               ,
           |    uni_shop_id            ,
           |    uni_id                 ,
           |    shop_id                ,
           |    plat_account           ,
           |    outer_product_id       ,
           |    outer_sku_id           ,
           |    product_id             ,
           |    sku_id                 ,
           |    product_num            ,
           |    price                  ,
           |    discount_fee           ,
           |    adjust_fee             ,
           |    total_fee              ,
           |    receive_payment        ,
           |    payment                ,
           |    discount_price         ,
           |    item_discount_rate     ,
           |    is_refund              ,
           |    refund_status          ,
           |    uni_refund_status      ,
           |    refund_fee             ,
           |    uni_refund_id          ,
           |    refund_id              ,
           |    consign_time           ,
           |    logistics_company      ,
           |    logistics_no           ,
           |    created                ,
           |    pay_time               ,
           |    uni_product_id         ,
           |    order_status           ,
           |    trade_type             ,
           |    modified               ,
           |    tenant                 ,
           |    tidb_modified          ,
           |    product_name           ,
           |    promotion_num          ,
           |    sku_properties_name    ,
           |    order_item_status      ,
           |    guider                 ,
           |    buyer_is_rate          ,
           |    part
           |  FROM
           |    ${result_table_view}
           |""".stripMargin)
  }

  private def getUnionKey(uniOrderItemId: String, tenant: String, partner: String): String = {
    s"${uniOrderItemId}|${tenant}|${partner}"
  }

  private def createTradeTable(tableEnvironment: StreamTableEnvironment): TableResult = {

    val sql =
      s"""
         |  CREATE TABLE IF NOT EXISTS `${tableName}`
         |  (
         |    de_duplication_key     string,
         |    uni_order_item_id      string,
         |    data_from              bigint,
         |    partner                string,
         |    uni_order_id           string,
         |    plat_code              string,
         |    order_item_id          string,
         |    order_id               string,
         |    uni_shop_id            string,
         |    uni_id                 string,
         |    shop_id                string,
         |    plat_account           string,
         |    outer_product_id       string,
         |    outer_sku_id           string,
         |    product_id             string,
         |    sku_id                 string,
         |    product_num            bigint,
         |    price                  double,
         |    discount_fee           double,
         |    adjust_fee             double,
         |    total_fee              double,
         |    receive_payment        double,
         |    payment                double,
         |    discount_price         double,
         |    item_discount_rate     double,
         |    is_refund              bigint,
         |    refund_status          string,
         |    uni_refund_status      string,
         |    refund_fee             double,
         |    uni_refund_id          string,
         |    refund_id              string,
         |    consign_time           string,
         |    logistics_company      string,
         |    logistics_no           string,
         |    created                string,
         |    pay_time               string,
         |    uni_product_id         string,
         |    order_status           string,
         |    trade_type             string,
         |    modified               string,
         |    tenant                 string,
         |    tidb_modified          string,
         |    product_name           string,
         |    promotion_num          bigint,
         |    sku_properties_name    string,
         |    order_item_status      string,
         |    guider                 string,
         |    buyer_is_rate          string,
         |    part                   string,
         |    PRIMARY KEY (`part`, `de_duplication_key`) NOT ENFORCED
         |  )
         |  PARTITIONED BY (`part`)
         |  WITH (
         |    'connector' = 'iceberg',
         |    'catalog-type' = 'hadoop',
         |    'catalog-name' = '${catalogName}',
         |    'catalog-database'='${namespaceName}',
         |    'warehouse' = '${warehousePath}',
         |    'write.wap.enabled' = 'true',
         |    'write.target-file-size-bytes' = '536870912',
         |    'write.metadata.delete-after-commit.enabled' = 'true',
         |    'write.metadata.previous-versions-max' = '100',
         |    'format-version' = '2',
         |    'commit.manifest-merge.enabled' = 'true',
         |    'history.expire.min-snapshots-to-keep' = '3',
         |    'history.expire.max-snapshot-age-ms' = '10800000',
         |    'write.upsert.enabled' = 'true'
         |  )
         |""".stripMargin

    tableEnvironment.executeSql(sql)
  }
}

