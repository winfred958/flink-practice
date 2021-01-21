# Flink On Yarn

## hadoop环境变量配置

- 方式一:全局环境配置, /etc/profile.d/
    - HADOOP_HOME
    - HADOOP_CONF_DIR
    - HADOOP_CLASSPATH=$(hadoop classpath)

方式二:[flink-conf.yaml](https://ci.apache.org/projects/flink/flink-docs-release-1.12/deployment/config.html#jvm-and-logging-options)

- env.hadoop.conf.dir:
- env.yarn.conf.dir:

## flink on yarn 基础配置

- rest.bind-address: 0.0.0.0
- rest.bind-port: 50100-50200

## flink on yarn HA

- yarn 配置, 重启后任务状态保持
    - [ResourceManger Restart](https://hadoop.apache.org/docs/r2.8.5/hadoop-yarn/hadoop-yarn-site/ResourceManagerRestart.html#Configurations)
    - ```yaml
      yarn.resourcemanager.recovery.enabled: true
      yarn.resourcemanager.store.class: org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore
      ```
- flink on yarn HA 配置
    - [yarn-cluster-high-availability](https://ci.apache.org/projects/flink/flink-docs-release-1.11/ops/jobmanager_high_availability.html#yarn-cluster-high-availability)
    - yarn-site.xml
        - 全局的applicationMaster重试次数
        - ```xml
          <property>
            <name>yarn.resourcemanager.am.max-attempts</name>
            <value>4</value>
            <description>
              The maximum number of application master execution attempts.
            </description>
          </property>
          ```
    - flink-conf.yaml
        - 重试次数
            - ```yaml
              yarn.application-attempts: 10
              ```
            - yarn.resourcemanager.am.max-attempts 是重试次数的上限
        - HA
            - ````yaml
              
              ````

## flink on yarn 其他配置
