package com.winfred.iceberg.stream

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.winfred.core.source.entity.raw.NoteSendRaw

object JacksonTest {

  def main(args: Array[String]): Unit = {
    val str =
      """
        | { "business_request_time": 1650001568004 }
        |""".stripMargin;

    val objectMapper = new ObjectMapper()
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val sendRaw = objectMapper.readValue(str, classOf[NoteSendRaw])

    println(s"${sendRaw}")
    println(s"${objectMapper.writeValueAsString(sendRaw)}")
  }

}
