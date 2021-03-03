# Queryable State (Beta)

```text
注意
目前(1.12) queryable state 的客户端 API 还在不断演进，不保证现有接口的稳定性。
在后续的 Flink 版本中有可能发生 API 变化
```

## Queryable State 架构

- QueryableStateClient
    - 默认, 允许在flink集群外部, 负责提交用户的请求.
- QueryableStateProxy
    - 运行在每个TaskManager上, 负责接收客户端请求,
    - 因为每个TaskManager会分配一些 key group, proxy会转发请求k到指定的 TaskManager
        - proxy 会询问 JobManager找到查询k所属的 key group 所在的TaskManager, 并转发到相应node, 查询state后返回.
- QueryableStateServer
    - 运行在TaskManager上, 负责服务本地的存储的state.

## [开启 Queryable state](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/dev/stream/state/queryable_state.html#%E6%BF%80%E6%B4%BB-queryable-state)

1. 将 flink-queryable-state-runtime_2.11-1.12.0.jar 从 Flink distribution 的 opt/ 目录拷贝到 lib/ 目录；
2. 将参数 queryable-state.enable 设置为
   true。详细信息以及其它配置可参考[文档 Configuration](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/deployment/config.html#queryable-state)
   .

## State 设置为 Queryable

```text
配置完集群开启Queryable state 还需要将 state 设置为 Queryable 才能对外可见
步骤
1. 创建 QueryableStateStream, 将会作为sink 将输入数据转化为 Queryable State.
2. 通过 stateDescriptor.setQueryable(String queryableStateName) 将 state 描述符所表示的 keyed state 设置成可查询的
```

KeyedStream 调用 asQueryableState(stateName, stateDescriptor)

## [client 查询 state](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/dev/stream/state/queryable_state.html#%E6%9F%A5%E8%AF%A2-state)
