package com.winfred.streamming.wordcount

import com.winfred.streamming.common.TestCase
import com.winfred.streamming.common.TestCase.SocketTestEntity
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.test.util.AbstractTestBase
import org.junit.Test

class WordCountTest extends AbstractTestBase {

  @Test
  def wordCountTest(): Unit = {
    val executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream: DataStream[SocketTestEntity] = TestCase.getDataStreamFromLocalSocket(executionEnvironment)

    dataStream.print()

    val result = WordCount.wordCount(dataStream)

    result.print

    executionEnvironment.execute("WordCount")
  }

}
