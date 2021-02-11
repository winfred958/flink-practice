# 算子

##                  

## [Windows](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/dev/stream/operators/windows.html)

- Keyed Windows
    - ```text
      stream
       .keyBy(...)                     <-  keyed versus non-keyed windows
       .window(...)                    <-  required: "assigner"
       [.trigger(...)]                 <-  optional: "trigger" (else default trigger)
       [.evictor(...)]                 <-  optional: "evictor" (else no evictor)
       [.allowedLateness(...)]         <-  optional: "lateness" (else zero)
       [.sideOutputLateData(...)]      <-  optional: "output tag" (else no side output for late data)
       .reduce/aggregate/fold/apply()  <-  required: "function"
       [.getSideOutput(...)]           <-  optional: "output tag"
      ```
- Non-Keyed Windows
    - ```text
      stream
        .windowAll(...)            <-  required: "assigner"
        [.trigger(...)]            <-  optional: "trigger" (else default trigger)
        [.evictor(...)]            <-  optional: "evictor" (else no evictor)
        [.allowedLateness(...)]    <-  optional: "lateness" (else zero)
        [.sideOutputLateData(...)] <-  optional: "output tag" (else no side output for late data)
        .reduce/aggregate/fold/apply()      <-  required: "function"
        [.getSideOutput(...)]      <-  optional: "output tag"
      ```

### trigger

```text
trigger 定义了window何时被  WindowFunction 处理.
每个 WindowAssigner 都有默认的 Trigger, 当然也可以使用自定义trigger  
```

interface Trigger

| 方法 | 描述 |
| :---- | :---- |
| TriggerResult onElement() | method is called for each element that is added to a window |
| TriggerResult onEventTime() | method is called when a registered event-time timer fires |
| TriggerResult onProcessingTime() | method is called when a registered processing-time timer fires |
| void onMerge() | method is relevant for stateful triggers and merges the states of two triggers when their corresponding windows merge, e.g. when using session windows. |
| void clear() |  method performs any action needed upon removal of the corresponding window |

enum TriggerResult

| 枚举 | 描述 |
| :---- | :---- |
| CONTINUE | do nothing, No action is taken on the window. |
| FIRE | 触发 WindowFunction ; trigger the computation, On FIRE, the window is evaluated and results are emitted. The window is not purged, though, all elements are retained. |
| PURGE | 清理window数据; All elements in the window are cleared and the window is discarded, without evaluating the window function or emitting any elements. |
| FIRE_AND_PURGE | 触发 WindowFunction获取结果,并清理window数据; trigger the computation and clear the elements in the window afterwards |

### evictor

### allowedLateness