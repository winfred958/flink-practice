# 概念

## [Concepts](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/concepts/index.html)

- FLink API
    - ![avatar](https://ci.apache.org/projects/flink/flink-docs-release-1.12/fig/levels_of_abstraction.svg)
    - 最底层的抽象为 stateful and timely stream
      processing,抽象实现为[ Process Function](https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/stream/operators/process_function.html)
        - Process Function
          被集成到了 [DataStream API](https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/datastream_api.html),
          允许用户在应用程序中自由地处理来自单流或多流的事件（数据），并提供具有全局一致性和容错保障的状态。
        - 用户可以在此层抽象中(Process Function)注册事件时间（event time）和处理时间（processing time）回调方法，从而允许程序可以实现复杂计算。
    - DataStream/DataSet (Core API)
        - 各种形式的用户自定义转换（transformations）、联接（joins）、聚合（aggregations）、窗口（windows）和状态（state）操作等
        - 和其他计算框架类似
    - Table API
        - Table API 是以表（Table）为中心的声明式编程（DSL）API
        - Table API 使用起来很简洁并且可以由各种类型的用户自定义函数扩展功能，但还是比 Core API 的表达能力差。
        - Table API 程序在执行之前还会使用优化器中的优化规则对用户编写的表达式进行优化。
        - 表和 DataStream/DataSet 可以进行无缝切换，Flink 允许用户在编写应用程序时将 **Table API 与 DataStream/DataSet API 混合使用**。
    - SQL
        - 这层抽象在语义和程序表达式上都类似于 Table API，但是其程序实现都是 SQL 查询表达式。
        - SQL 查询语句可以在 Table API 中定义的表上执行。

## [Stateful Stream Processing](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/concepts/stateful-stream-processing.html)

### 什么是State?

- Flink 从 checkpoint 和 savepoint 获取状态, 保证容错.
- [Queryable state](https://ci.apache.org/projects/flink/flink-docs-release-1.12/dev/stream/state/queryable_state.html) allows you to access state from outside of Flink during runtime.
- Flink 提供了不同的 [State Backends](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/ops/state/state_backends.html) 支持存储方式和存储位置

### Keyed State

- xxx
    - ![avatar](https://ci.apache.org/projects/flink/flink-docs-release-1.12/fig/state_partitioning.svg)

### State Persistence

- #### Checkpoint

- #### State Backends

- #### Savepoint

- #### Exactly Once & At Least Once

## [实时流处理](https://ci.apache.org/projects/flink/flink-docs-release-1.12/concepts/timely-stream-processing.html)

### Event time & Processing Time

### Event Time & Watermarks

### Window

## [Glossary](https://ci.apache.org/projects/flink/flink-docs-release-1.12/concepts/glossary.html)

