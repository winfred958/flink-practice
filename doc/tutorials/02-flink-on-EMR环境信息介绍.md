# Flink on EMR 环境信息

## 1. EMR & Flink & hadoop 版本对应关系

| EMR   | flink  | hadoop | scala |
|:------|:-------|:-------|:------|
| 2.3.0 | 1.9.2  | 2.8.5  | 2.11  |
| 2.2.1 | 1.10.1 | 2.8.5  | 2.11  |
| 2.2.0 | 1.9.2  | 2.8.5  | 2.11  |
| 3.0.0 | 1.8.1  | 3.1.2  | 2.11  |
| 2.1.0 | 1.4.2  | 2.8.4  | ?     |

## 2. Flink 部署目录

- EMR 上 flink 部署目录
    - FLINK_HOME=/usr/local/service/flink
- 您可以在EMR控制台更改配置, 然后下发配置

## 3. 推荐使用方式

- 在EMR上推荐您使用flink on
  yarn模式 [YARN Setup](https://ci.apache.org/projects/flink/flink-docs-release-1.10/ops/deployment/yarn_setup.html)
    - 你可以配置自己的flink client, (编译flink版本, 配置环境变量即可)
- job 提交方式
    - flink on yarn session (不推荐, 多个job共有1个jobManager, 耦合严重)
    - flink on yarn single job
        - [官方文档](https://ci.apache.org/projects/flink/flink-docs-release-1.10/ops/deployment/yarn_setup.html#run-a-single-flink-job-on-yarn)
        - 推荐使用长命令方式, 示例
            - ```shell script
              flink run \
                --class com.datamining.streaming.base.RealTimeRawHandler \
                --jobmanager yarn-cluster \
                --yarnslots 4 \
                --yarnjobManagerMemory 2048 \
                --yarntaskManagerMemory 2048 \
                --parallelism 12 \
                --detached  \
                --yarnname RealTimeRawHandler \
                /home/hadoop/user-project/lib/xxxx.jar
              ```

## 4. 需要您关注的Flink配置 ()

| key                       | 简述                          | 默认值           | 推荐                                                        |
|:--------------------------|:----------------------------|:--------------|:----------------------------------------------------------|
| jobmanager.rpc.address    |                             | none          | 0.0.0.0                                                   |
| jobmanager.rpc.port       |                             | 6123          |                                                           |
| jobmanager.heap.size      | JobManager heap size        | 1024m         | flink run 参数指定                                            |
| taskmanager.heap.size     | TaskManager heap size       | 1024m         | flink run 参数指定                                            |
| parallelism.default       | 并行度                         | 1             | flink run 参数指定                                            |
| rest.bind-port            | 会覆盖 rest.port的值, **建议配置这个** | 8081          | 为了避免, 一个node上启动多个JobManager造成冲突, 建议配置成区间, 例如“50100-50200” |
| rest.bind-address         |                             | none          | 0.0.0.0                                                   |
| classloader.resolve-order | 定义优先加载的依赖                   | "child-first" | "child-first"                                             |

