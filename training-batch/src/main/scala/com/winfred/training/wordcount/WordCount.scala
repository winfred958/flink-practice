package com.winfred.training.wordcount

import com.winfred.core.utils.IkAnalyzerUtils
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment, _}

object WordCount {

  def main(args: Array[String]): Unit = {

    val environment: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    val dataSet: DataSet[String] = getSourceDataSet(environment)

    val outputPath = s"D:/tmp/flink-test-output/wc-test"

    dataSet
      .filter(line => {
        StringUtils.isNoneBlank(line)
      })
      .map(line => {
        IkAnalyzerUtils.getTerms(line, true)
      })
      .flatMap(list => {
        list.toArray
      })
      .map(term => {
        (String.valueOf(term), 1)
      })
      .print()
    //      .groupBy(0)
    //      .sum(1)
    //      .sortPartition(1, Order.DESCENDING)
    //      .first(10)
    //      .map(x => {
    //        s"${x._1}: ${x._2}"
    //      })
    //      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE)

    environment.execute("wc-test")
  }


  def getSourceDataSet(environment: ExecutionEnvironment): DataSet[String] = {
    val tmpDataPath = Thread.currentThread().getContextClassLoader.getResource("data/blog-context.text").toString
    val dataSet: DataSet[String] = environment
      .readTextFile(tmpDataPath)
    dataSet
  }
}
