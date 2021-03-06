# Flink 读写 ckafka

## 1. 网络互通

- 确保EMR和ckafka在同一个vpc或配置路由转发策略

## 2. ckafka client 选择

- 目前大多使用kafka 0.11.0.2 + 版本, 可以和无缝ckafka兼容,
  详细请看[CKafka 与开源 Kafka 兼容性说明](https://cloud.tencent.com/document/product/597/11173)

## 3. demo (kafka client 2.2.x)

```scala 3
import java.text.SimpleDateFormat
import java.util.{Calendar, Properties}

import com.winfred.core.config.KafkaConfig
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer


object CKafkaExample {

  var auto_offset_reset: String = "earliest"

  val groupId: String = this.getClass.getCanonicalName
  val sourceTopic: String = "ckafka_test_raw"
  val sinkTopic: String = "ckafka_test_target"

  def getKafkaSource(topic: String, groupId: String): FlinkKafkaConsumer[String] = {
    val kafkaConfigEntity = KafkaConfig.getConfigEntity()
    val bootstrapServers = kafkaConfigEntity.getKafka.getConsumer.getBootstrapServers

    val properties = new Properties()
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500")
    properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000")
    properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, auto_offset_reset)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)

    val source: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema(), properties)
    source
  }

  def getKafkaSink(topic: String): FlinkKafkaProducer[String] = {
    val kafkaConfigEntity = KafkaConfig.getConfigEntity()
    val bootstrapServers = kafkaConfigEntity.getKafka.getProducer.getBootstrapServers
    val properties = new Properties()
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    properties.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "900000")
    properties.setProperty(ProducerConfig.ACKS_CONFIG, "0")
    properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "16384")

    val sink: FlinkKafkaProducer[String] = new FlinkKafkaProducer[String](topic, new SimpleStringSchema(), properties)
    sink.setWriteTimestampToKafka(true)
    sink
  }

  def main(args: Array[String]): Unit = {

    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    executionEnvironment
      .enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE)

    import org.apache.flink.streaming.api.scala._

    // add source
    val dataSource: DataStream[String] = executionEnvironment
      .addSource(
        getKafkaSource(
          topic = sourceTopic, groupId = groupId
        )
      )
      .filter(str => {
        StringUtils.isNotBlank(str)
      })

    // data process
    val result: DataStream[String] = dataSource
      .map(str => {
        ("str", 1L)
      })
      .keyBy(0)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
      .reduce((a, b) => {
        (a._1, a._2 + b._2)
      })
      .map(entity => {
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val datetimeStr = dateFormat.format(Calendar.getInstance().getTime)
        s"""{"${datetimeStr}": ${entity._2}}"""
      })

    // add sink
    result
      .addSink(
        getKafkaSink(topic = sinkTopic)
      )

    executionEnvironment
      .execute("CKafkaExample")
  }

```
