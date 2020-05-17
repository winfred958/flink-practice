package com.winfred.training.fs

import org.apache.flink.api.java.io.TypeSerializerOutputFormat
import org.apache.flink.api.scala.hadoop.mapreduce.HadoopOutputFormat
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.formats.parquet.ParquetWriterFactory
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, OutputFormat}
import org.apache.parquet.hadoop.ParquetOutputFormat
import org.apache.parquet.hadoop.codec.SnappyCodec

import scala.collection.mutable.ListBuffer

object BatchBucketWrite {

  def main(args: Array[String]): Unit = {

    import org.apache.flink.api.scala._

    val tmpDataPath = Thread.currentThread().getContextClassLoader.getResource("data/blog-context.text").toString
    val batchEnv: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

    val strDataSet: DataSet[String] = batchEnv.readTextFile(tmpDataPath)

    strDataSet.print()

    val dataSet: DataSet[TestEntity] = getDataSource(executionEnvironment = batchEnv)
    //
    //    val table = batchTableEnvironment.fromDataSet(dataSet)


    val parquetWriterFactory: ParquetWriterFactory[TestEntity] = ParquetAvroWriters.forReflectRecord(classOf[TestEntity])

    dataSet
      .print()

    //    sinkToLocalSerializerFile(tmpDataPath, dataSet)

    sinkToHdfs(dataSet, new Path("I:\\tmp\\flink-text-output-test"))

    batchEnv.execute("file_system_test")
  }


  private def sinkToLocalSerializerFile(tmpDataPath: String, dataSet: DataSet[TestEntity]) = {
    import org.apache.flink.api.scala._

    val typeSerializerOutputFormat = new TypeSerializerOutputFormat[String]()

    dataSet
      .map(entity => {
        s"${entity.key} = ${entity.value}\n"
      })
      .write(
        typeSerializerOutputFormat,
        s"${tmpDataPath}/test_1",
        FileSystem.WriteMode.OVERWRITE
      )
  }

  def getDataSource(executionEnvironment: ExecutionEnvironment): DataSet[TestEntity] = {
    import org.apache.flink.api.scala._
    val entities: ListBuffer[TestEntity] = new ListBuffer[TestEntity]
    for (k <- 0.until(10)) {
      for (v <- 0.until(10)) {
        entities.+=(TestEntity(key = k, value = s"${k} = ${v}"))
      }
    }
    executionEnvironment.fromCollection(entities)
  }


  /**
   *
   * @param dataSet
   * @param path
   */
  def sinkToHdfs(dataSet: DataSet[TestEntity], path: Path): Unit = {

    val outputFormat: OutputFormat[Void, Text] = new ParquetOutputFormat[Text]()


    val jobConf = new JobConf()
    jobConf.setCompressMapOutput(true)
    jobConf.setMapOutputCompressorClass(classOf[SnappyCodec])

    val job = Job.getInstance(jobConf)

    val configuration = job.getConfiguration

    FileOutputFormat.setOutputPath(job, path)

    val hadoopOutputFormat: HadoopOutputFormat[Void, Text] = new HadoopOutputFormat[Void, Text](
      mapredOutputFormat = outputFormat,
      job = job
    )

    import org.apache.flink.api.scala._
    dataSet
      .map(entity => {
        val v: Void = null
        (v, new Text(s"${entity.key} = ${entity.value}"))
      })
      .output(hadoopOutputFormat)
  }

  case class TestEntity(
                         key: Long,
                         value: String
                       )

}
