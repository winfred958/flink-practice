# 用户自定义Function

## Function (MapFunction)

## **Rich Function** (RichMapFunction)

**Rich Function 提供 open, close, getRuntimeContext, setRuntimeContext**, 可以满足以下功能:

- 参数化function
- 创建和确定State, 参见 [03-状态&容错的使用](03-状态&容错的使用.md)
- 访问广播变量, 参见 [03-状态&容错的使用](03-状态&容错的使用.md)
- 访问运行时(例如累加器和计数器), 以及迭代器相关信息

### [累加器和计数器的使用](https://ci.apache.org/projects/flink/flink-docs-release-1.12/zh/dev/user_defined_functions.html#%E7%B4%AF%E5%8A%A0%E5%99%A8%E5%92%8C%E8%AE%A1%E6%95%B0%E5%99%A8)


