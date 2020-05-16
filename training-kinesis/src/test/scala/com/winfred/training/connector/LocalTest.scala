package com.winfred.training.connector

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._

import scala.collection.mutable.ListBuffer

object LocalTest {

  def main(args: Array[String]): Unit = {
    val environment = ExecutionEnvironment.createLocalEnvironment()

    environment
      .fromCollection(getTestData())
      .map(x => {
        s"${x}==${x}"
      })
      .print()

  }


  def getTestData(): List[String] = {
    val seqToStrings = new ListBuffer[String]()

    seqToStrings.+=("a")
    seqToStrings.+=("b")
    seqToStrings.+=("c")
    seqToStrings.toList
  }
}
