- MapFunction 为例, 这里使用了adaptor模式(Function和实际调用使用了adaptor模式, StreamOperator -> 具体的StreamXXX 使用了装饰器模式)
    - ![avatar](images/uml-Function-Operator.png)
    - 用户设置MapFunction
        - DataStream.map(new MapFunction(){...})时
    - 用户自定义MapFunction最终被调用源码位置为 [StreamMap](https://github.com/apache/flink/blob/master/flink-streaming-java/src/main/java/org/apache/flink/streaming/api/operators/StreamMap.java#L26)
        - ```java
          @Internal
          public class StreamMap<IN, OUT> extends AbstractUdfStreamOperator<OUT, MapFunction<IN, OUT>>
                implements OneInputStreamOperator<IN, OUT> {
          
              private static final long serialVersionUID = 1L;
          
              public StreamMap(MapFunction<IN, OUT> mapper) {
                  super(mapper);
                  chainingStrategy = ChainingStrategy.ALWAYS;
              }
          
              @Override
              public void processElement(StreamRecord<IN> element) throws Exception {
                  output.collect(element.replace(userFunction.map(element.getValue())));
              }
          }
          ``` 