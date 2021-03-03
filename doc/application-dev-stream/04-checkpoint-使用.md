# checkpoint

## 开启checkpoint

- 默认情况 checkpoint 是禁用的. 开启方式 StreamExecutionEnvironment.enableCheckpointing(interval, CheckpointingMode)
    - ```java
      StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

      // 每 1000ms 开始一次 checkpoint
      env.enableCheckpointing(1000);
      
      // 高级选项：
      // 设置模式为精确一次 (这是默认值)
      env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
      
      // 确认 checkpoints 之间的时间会进行 500 ms
      env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
      
      // Checkpoint 必须在一分钟内完成，否则就会被抛弃
      env.getCheckpointConfig().setCheckpointTimeout(60000);
      
      // 同一时间只允许一个 checkpoint 进行
      env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
      
      // 开启在 job 中止后仍然保留的 externalized checkpoints
      env.getCheckpointConfig().enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
      ```

## 设置checkpoint State Backend

## task failover

```text
重启策略
恢复策略
```

### Restart Strategy (重启策略)

```text
Task 发生故障时, Flink 需要重启出错的Task以及其他受到影响的Task, 以使得作业恢复到正常执行状态.
Flink 通过 restart-strategy 和 failover strategy 来控制Task重启:
   restart-strategy 决定是否可以重启以及重启的间隔
   failover strategy 决定那些Task需要重启
```

- Fixed Delay Restart Strategy
    - 固定延时重启策略, 按照给定的重启次数和间隔, 如果尝试超过了给定的最大次数, 作业将最终失败.
    - 全局配置方式, flink-conf.yaml
      ```yaml
      restart-strategy: fixed-delay
      restart-strategy.fixed-delay.attempts: 3
      restart-strategy.fixed-delay.delay: 10s
      ```
      | key | default | type | description |
                                                                                    | :--- | :--- | :--- | :--- |
      | restart-strategy.fixed-delay.attempts | 1 | Integer | 重启次数 |
      | restart-strategy.fixed-delay.delay | 1s | Duration | 重启间隔 |
    - 程序中独立配置
        - ```java
          ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
          env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
            3, // 尝试重启的次数
            Time.of(10, TimeUnit.SECONDS) // 延时
          ));
          ```

- Failure Rate Restart Strategy
    - 每次restart间隔固定, 在固定的周期内, 超过重启次数限制
    - 全局配置方式, flink-conf.yaml
      ```yaml
      restart-strategy: failure-rate
      restart-strategy.failure-rate.delay: 10 s
      restart-strategy.failure-rate.max-failures-per-interval: 3
      restart-strategy.failure-rate.failure-rate-interval: 5 min
      ```
      | key | default | type | description |
                                                            | :--- | :--- | :--- | :--- |
      | restart-strategy.failure-rate.delay | 1s | Integer | restart 间隔 |
      | restart-strategy.failure-rate.failure-rate-interval | 1 min | Duration | 周期 |
      | restart-strategy.failure-rate.max-failures-per-interval | 1 | Duration | 周期中restart阈值 |
    - 程序中独立配置
        - ```java
          ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
          env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
            3, // 每个时间间隔的最大故障次数
            Time.of(5, TimeUnit.MINUTES), // 测量故障率的时间间隔
            Time.of(10, TimeUnit.SECONDS) // 延时
          ));
          ```

- No Restart Strategy
    - 作业直接失败, 不重启
    - 全局配置方式, flink-conf.yaml
      ```yaml
      restart-strategy: none
      ```

### Failover Strategy (恢复策略)

- 配置方式, flink-conf.yaml
    - jobmanager.execution.failover-strategy
        - full
            - 全图重启
        - region
            - 基于region的局部重启

#### Restart All Failover Strategy

- jobmanager.execution.failover-strategy: full
    - Task 发生故障时会重启作业中所有的Task进行故障恢复

#### Restart Pipelined Region Failover Strategy

- jobmanager.execution.failover-strategy: region
    - 作业中所有task划分为数个region. 当task发生故障时, 会尝试找出进行故障需要重启的最小region集合. 重启部分task.
- Region 指 Pipelined 形式进行数据交换的Task集合
    - DataStream 和 流式 Table/SQL 作业的所有数据交换都是 Pipelined 形式的。
    - 批处理式 Table/SQL 作业的所有数据交换默认都是 Batch 形式的。
    - DataSet 作业中的数据交换形式会根据 ExecutionConfig 中配置的 ExecutionMode 决定。
- 需要重启的 Region 的判断逻辑如下：
    - 出错 Task 所在 Region 需要重启。
    - 如果要重启的 Region 需要消费的数据有部分无法访问（丢失或损坏），产出该部分数据的 Region 也需要重启。
    - 需要重启的 Region 的下游 Region 也需要重启。这是出于保障数据一致性的考虑，因为一些非确定性的计算或者分发会导致同一个 Result Partition 每次产生时包含的数据都不相同。