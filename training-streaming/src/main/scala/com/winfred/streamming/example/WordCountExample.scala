package com.winfred.streamming.example

import com.winfred.core.entity.log.EventEntity
import com.winfred.core.source.DataMockSource
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger

import java.util.UUID

object WordCountExample {

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream = getFromMockSource(executionEnvironment = executionEnvironment)

    val wordCountStream: DataStream[Word] = wordCount(dataStream)

    import org.apache.flink.streaming.api.scala._

    wordCountStream
      .map(entity => {
        (entity.word, entity.count)
      })
      .keyBy(entity => {
        entity._1
      })
      .reduce((a, b) => {
        (a._1, a._2 + b._2)
      })
      .print()

    wordCountStream.print()


    executionEnvironment.execute("Socket Window WordCount")
  }

  def wordCount(dataStream: DataStream[EventEntity]): DataStream[Word] = {

    import org.apache.flink.streaming.api.scala._

    dataStream
      .map(entity => {
        entity.getUuid
      })
      .map(term => {
        Word(term, 1L)
      })
      .keyBy(entity => {
        entity.word
      })
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .trigger(ProcessingTimeTrigger.create())
      .reduce((a, b) => {
        Word(a.word, b.count + a.count)
      })
  }

  /**
   * need install netcat;
   * nc -l -p 9999
   *
   * @param executionEnvironment
   * @param hostname
   * @param port
   * @return
   */
  def getDataStreamFromLocalSocket(
                                    executionEnvironment: StreamExecutionEnvironment,
                                    hostname: String = "127.0.0.1",
                                    port: Int = 9999
                                  ): DataStream[SocketTestEntity] = {
    import org.apache.flink.streaming.api.scala._

    executionEnvironment
      .socketTextStream(hostname, port)
      .map(str => {
        SocketTestEntity(
          text = str
        )
      })
      .assignTimestampsAndWatermarks(new AscendingTimestampExtractor[SocketTestEntity] {
        override def extractAscendingTimestamp(element: SocketTestEntity): Long = {
          element.server_time
        }
      })
  }

  def getFromMockSource(executionEnvironment: StreamExecutionEnvironment): DataStream[EventEntity] = {
    import org.apache.flink.streaming.api.scala._
    executionEnvironment
      .addSource(new DataMockSource(2, 20))
  }

  case class SocketTestEntity(
                               uuid: String = UUID.randomUUID().toString,
                               server_time: Long = System.currentTimeMillis(),
                               text: String = ""
                             )

  case class Word(
                   word: String,
                   count: Long
                 )

}
