package com.winfred.streamming.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.winfred.core.source.entity.NoteReceiptEntity

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

object TestDate {

  private val objectMapper = new ObjectMapper()
  val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

  def main(args: Array[String]): Unit = {
    objectMapper.findAndRegisterModules();
    val note = new NoteReceiptEntity()

    val now = LocalDateTime.now()
    note.setSend_time(now)
    note.setReceive_time(now)
    val str = objectMapper.writeValueAsString(note)

    println(str)
    println(now.format(DateTimeFormatter.ISO_DATE_TIME.withZone(zoneId)))
  }

}
