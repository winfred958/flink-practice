package com.winfred.iceberg.stream

import com.fasterxml.jackson.databind.ObjectMapper
import com.winfred.core.source.entity.raw.NoteSendRaw

object JacksonTest {

  def main(args: Array[String]): Unit = {
    val str =
      """
        | { "business_request_time": 1650001568004 }
        |""".stripMargin;

    val objectMapper = new ObjectMapper()

    val sendRaw = objectMapper.readValue(str, classOf[NoteSendRaw])
    println(s"${sendRaw}")
  }

}
