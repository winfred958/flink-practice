# Flink on EMR 环境信息
## 1. EMR & Flink & hadoop 版本对应关系

| EMR | flink | hadoop | scala |
| :--- | :--- | :--- | :--- |
| 2.3.0 | 1.9.2 | 2.8.5 | 2.11 |
| 2.2.1 | 1.10.1| 2.8.5 | 2.11 |
| 2.2.0 | 1.9.2 | 2.8.5 | 2.11 |
| 3.0.0 | 1.8.1 | 3.1.2 | 2.11 |
| 2.1.0 | 1.4.2 | 2.8.4 | ? |

## 2. Flink 部署目录
- EMR 上 flink 部署目录
    - FLINK_HOME=/usr/local/service/flink
## 3. 推荐使用方式
- 在EMR上推荐您使用flink on yarn模式 [YARN Setup](https://ci.apache.org/projects/flink/flink-docs-release-1.10/ops/deployment/yarn_setup.html)
    - 你可以配置自己的flink client, (编译flink版本, 配置环境变量即可)
- job 提交方式
    - flink on yarn session (不推荐, 多个job共有1个jobManager, 耦合严重)
    - flink on yarn single job
        - [官方文档](https://ci.apache.org/projects/flink/flink-docs-release-1.10/ops/deployment/yarn_setup.html#run-a-single-flink-job-on-yarn)
## 4. 需要您关注的Flink配置
