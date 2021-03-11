# 重要的数据结构

## StreamGraph & StreamNode

- StreamGraph
    - ```java
      /**
       * Class representing the streaming topology. It contains all the information necessary to build the
       * jobgraph for the execution.
       */
      @Internal
      public class StreamGraph implements Pipeline {
          private String jobName;
          
          /**
           * id -> StreamNode
           */
          private Map<Integer, StreamNode> streamNodes;
          private Set<Integer> sources;
          private Set<Integer> sinks;
      }
      ```
- StreamNode
    - ```java
      /** Class representing the operators in the streaming programs, with all their properties. */
      @Internal
      public class StreamNode {
          private final int id;
          private int parallelism;
          /**
           * Maximum parallelism for this stream node. The maximum parallelism is the upper limit for
           * dynamic scaling and the number of key groups used for partitioned state.
           */
          private int maxParallelism;

          private ResourceSpec minResources = ResourceSpec.DEFAULT;
          private ResourceSpec preferredResources = ResourceSpec.DEFAULT;
          private final Map<ManagedMemoryUseCase, Integer> managedMemoryOperatorScopeUseCaseWeights = new HashMap<>();
          private final Set<ManagedMemoryUseCase> managedMemorySlotScopeUseCases = new HashSet<>();
          private long bufferTimeout;
          private final String operatorName;
          private @Nullable String slotSharingGroup;
          private @Nullable String coLocationGroup;
          private KeySelector<?, ?>[] statePartitioners = new KeySelector[0];
          private TypeSerializer<?> stateKeySerializer;

          private StreamOperatorFactory<?> operatorFactory;
          private TypeSerializer<?>[] typeSerializersIn = new TypeSerializer[0];
          private TypeSerializer<?> typeSerializerOut;
      
          /**
           * StreamGraph Edge
           */
          private List<StreamEdge> inEdges = new ArrayList<StreamEdge>();
          private List<StreamEdge> outEdges = new ArrayList<StreamEdge>();

          private final Class<? extends AbstractInvokable> jobVertexClass;

          private InputFormat<?, ?> inputFormat;
          private OutputFormat<?> outputFormat;

          private String transformationUID;
          private String userHash;

          private final Map<Integer, StreamConfig.InputRequirement> inputRequirements = new HashMap<>();
      }
      ```

## JobGraph & JobVertex