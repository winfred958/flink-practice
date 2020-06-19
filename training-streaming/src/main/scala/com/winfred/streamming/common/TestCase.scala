package com.winfred.streamming.common

import java.util.UUID

import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object TestCase {

  /**
   * windows 10 need install netcat; nc -l -p 9999
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

    executionEnvironment.socketTextStream(hostname, port)
      .map(str => {
        SocketTestEntity(
          text = str
        )
      })
      .assignTimestampsAndWatermarks(new AscendingTimestampExtractor[SocketTestEntity] {
        override def extractAscendingTimestamp(element: SocketTestEntity): Long = {
          element.server_time - 50000
        }
      })
  }

  case class SocketTestEntity(
                               uuid: String = UUID.randomUUID().toString,
                               server_time: Long = System.currentTimeMillis(),
                               text: String = ""
                             )


}
