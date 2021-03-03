# training-streaming

## 功能介绍

1. CKafkaMockSource 生成测试数据写入kafka topic-1
2. CKafkaExample 消费 kafka topic-1 生成的数据计算并写回 kafka topic-2
3. HbaseExample 生成测试数据写入 hbase

## 运行方式

- 打包得到 releases 压缩包
    - build命令
      ```shell
      cd flink-practice
      mvn package
      ```
    - build 完毕 会在 flink-practice/training-streaming/target/releases 目录下生成压缩包
- 解压
    - 解压 flink-practice/training-streaming/target/releases/xxx.tar.gz
        - tar -zxvf xxx.tar.gz
    - 目录结构
      ```text
      training-streaming/bin #执行脚本
      training-streaming/lib #jar 包
      ```
    - 执行脚本说明

      | 名称 | 入口类 | 功能 |
                        | :---- | :---- | :---- |
      | bin/kafka_mock_source.sh | [CKafkaMockSource](src/main/scala/com/winfred/streamming/ckafka/CKafkaMockSource.scala) | mock 测试数据 sink 到kafka (脚本中需要指定topic) |
      | bin/kafka_source_sink_test.sh | [CKafkaExample](src/main/scala/com/winfred/streamming/example/CKafkaExample.scala) | 测试kafka source sink (同上)|
      | bin/hbase-sink-example.sh | [HbaseExample](src/main/scala/com/winfred/streamming/example/HbaseExample.scala) | 测试hbase sink (需要提前建表, 脚本中指定 zookeeper地址) |
    

