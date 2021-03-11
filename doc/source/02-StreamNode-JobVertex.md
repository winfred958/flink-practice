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
      
          // 仅列举部分字段
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
      
          /**
           * 
           */
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

- JobGraph
    - ```java
      /**
      * The JobGraph represents a Flink dataflow program, at the low level that the JobManager accepts.
      * All programs from higher level APIs are transformed into JobGraphs.
      *
      * <p>The JobGraph is a graph of vertices and intermediate results that are connected together to
      * form a DAG. Note that iterations (feedback edges) are currently not encoded inside the JobGraph
      * but inside certain special vertices that establish the feedback channel amongst themselves.
      *
      * <p>The JobGraph defines the job-wide configuration settings, while each vertex and intermediate
      * result define the characteristics of the concrete operation and intermediate data.
        */
      public class JobGraph implements Serializable {
      
          /** jobId -> JobVertex */
          /** List of task vertices included in this job graph. */
          private final Map<JobVertexID, JobVertex> taskVertices = new LinkedHashMap<JobVertexID, JobVertex>();
      
          /** The job configuration attached to this job. */
          private final Configuration jobConfiguration = new Configuration();
        
          /** ID of this job. May be set if specific job id is desired (e.g. session management) */
          private JobID jobID;
        
          /** Name of this job. */
          private final String jobName;
          
          /** Set of JAR files required to run this job. */
          private final List<Path> userJars = new ArrayList<Path>();
          
          /** Set of custom files required to run this job. */
          private final Map<String, DistributedCache.DistributedCacheEntry> userArtifacts = new HashMap<>();
        
          /** Set of blob keys identifying the JAR files required to run this job. */
          private final List<PermanentBlobKey> userJarBlobKeys = new ArrayList<>();
        
          /** List of classpaths required to run this job. */
          private List<URL> classpaths = Collections.emptyList();
      
          // 这里仅列举部分字段
      }
      ```
- JobVertex
    - ```java

      ```