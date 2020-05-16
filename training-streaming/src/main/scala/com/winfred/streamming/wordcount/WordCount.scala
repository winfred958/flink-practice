package com.winfred.streamming.wordcount

import com.winfred.streamming.common.TestCase.SocketTestEntity
import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object WordCount {

  def wordCount(dataStream: DataStream[SocketTestEntity]): DataStream[(String, Long)] = {

    import org.apache.flink.streaming.api.scala._

    dataStream
      .map(e => {
        (e.text, 1L)
      })
      .keyBy(0)
      .window(SlidingProcessingTimeWindows.of(Time.seconds(60), Time.seconds(5)))
      .reduce(new ReduceFunction[(String, Long)] {
        override def reduce(value1: (String, Long), value2: (String, Long)): (String, Long) = {
          (value1._1, value1._2 + value2._2)
        }
      })
  }
}
