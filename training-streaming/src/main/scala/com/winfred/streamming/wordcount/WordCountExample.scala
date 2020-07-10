package com.winfred.streamming.wordcount

import java.util.UUID

import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.evictors.DeltaEvictor
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger

object WordCountExample {

  def main(args: Array[String]): Unit = {
    val executionEnvironment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val socketDataStream: DataStream[SocketTestEntity] = getDataStreamFromLocalSocket(executionEnvironment = executionEnvironment)

    val wordCountStream: DataStream[Word] = wordCount(socketDataStream)

    import org.apache.flink.streaming.api.scala._

    wordCountStream
      .map(entity => {
        (entity.word, entity.count)
      })
      .keyBy(0)
      .reduce((a, b) => {
        (a._1, a._2 + b._2)
      })
      .print()

    wordCountStream.print()


    executionEnvironment.execute("Socket Window WordCount")
  }

  def wordCount(dataStream: DataStream[SocketTestEntity]): DataStream[Word] = {

    import org.apache.flink.streaming.api.scala._

    dataStream
      .map(entity => {
        entity.text
      })
      .flatMap(text => {
        text.split("\\W+")
      })
      .filter(str => {
        StringUtils.isNotBlank(str)
      })
      .map(term => {
        Word(term, 1L)
      })
      .keyBy("word")
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
