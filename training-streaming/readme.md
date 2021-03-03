# 运行方式

- 打包得到 releases 压缩包
    - build命令
      ```shell
      cd flink-practice
      mvn package
      ```
    - build 完毕 会在 flink-practice/training-streaming/target/releases 目录下生成压缩包
- 解压
    - 解压 flink-practice/training-streaming/target/releases/xxx.gzip
    - 目录结构
      ```text
      training-streaming/bin #执行脚本
      training-streaming/lib #jar 包
      ```
    - 执行脚本说明

      | 名称 | 入口类 | 功能 |
                        | :---- | :---- | :---- |
      | kafka_mock_source.sh | [CKafkaMockSource](src/main/scala/com/winfred/streamming/ckafka/CKafkaMockSource.scala) | mock 测试数据 sink 到kafka (脚本中需要指定topic) |
      | kafka_source_sink_test.sh | [CKafkaExample](src/main/scala/com/winfred/streamming/example/CKafkaExample.scala) | 测试kafka source sink (同上)|
      | hbase-sink-example.sh | [HbaseExample](src/main/scala/com/winfred/streamming/example/HbaseExample.scala) | 测试hbase sink (需要提前建表, 脚本中指定 zookeeper地址) |