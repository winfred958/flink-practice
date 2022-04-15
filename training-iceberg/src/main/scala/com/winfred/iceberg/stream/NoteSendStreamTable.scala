package com.winfred.iceberg.stream

import com.fasterxml.jackson.databind.{ObjectMapper, SerializerProvider}
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.winfred.core.config.KafkaConfig

import java.sql.Timestamp

object NoteSendStreamTable {




  def main(args: Array[String]): Unit = {
    val kafkaConfigEntity = KafkaConfig.getConfigEntity()
    val consumer = kafkaConfigEntity.getKafka.getConsumer


    val objectMapper = new ObjectMapper()
    objectMapper

//      .acceptJsonFormatVisitor(classOf[Timestamp], new JsonFormatVisitorWrapper {
//        override def getProvider: SerializerProvider = {
//          return SerializerProvider.DEFAULT_NULL_KEY_SERIALIZER
//        }
//
//        override def setProvider(provider: SerializerProvider): Unit = {
//
//        }
//      })

  }
}
