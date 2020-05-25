package com.winfred.training.wc

import com.winfred.core.utils.{ArgsHandler, IkAnalyzerUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment, _}
import org.slf4j.{Logger, LoggerFactory}

object WordCountTest {

  val log: Logger = LoggerFactory.getLogger(this.getClass);

  def main(args: Array[String]): Unit = {

    val environment: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    val dataSet: DataSet[String] = getSourceDataSet(environment, args)

    val outputPath = s"D:/tmp/flink-test-output/wc-test"

    val reslut = dataSet
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

    reslut
      .map(x => {
        System.out.println(s"${x._1}, ${x._2}")
      })
    //      .groupBy(0)
    //      .sum(1)
    //      .sortPartition(1, Order.DESCENDING)
    //      .first(10)
    //      .map(x => {
    //        s"${x._1}: ${x._2}"
    //      })
    //      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE)

    //    environment.execute("wc-test")
  }


  def getSourceDataSet(environment: ExecutionEnvironment, args: Array[String]): DataSet[String] = {
    var inputPath = ArgsHandler.getArgsParam(args, "input-path")
    log.info("inputPath = {}", inputPath)
    if (StringUtils.isBlank(inputPath)) {
      inputPath = Thread.currentThread().getContextClassLoader.getResource("data/blog-context.text").toString
    }
    val dataSet: DataSet[String] = environment
      .readTextFile(inputPath)
    dataSet
  }
}
