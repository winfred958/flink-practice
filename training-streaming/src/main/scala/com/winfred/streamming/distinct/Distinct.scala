package com.winfred.streamming.distinct

import com.winfred.streamming.common.TestCase.SocketTestEntity
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.util.Collector

object Distinct {

  def distinct(dataStream: DataStream[SocketTestEntity]): Unit = {

  }

  class DistinctFunction extends KeyedProcessFunction[SocketTestEntity, SocketTestEntity, (String, Long)] {
    override def processElement(value: SocketTestEntity, ctx: KeyedProcessFunction[SocketTestEntity, SocketTestEntity, (String, Long)]#Context, out: Collector[(String, Long)]): Unit = ???
  }

}


