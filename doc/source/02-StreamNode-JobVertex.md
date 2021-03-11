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
      /** The base class for job vertexes. */
      public class JobVertex implements java.io.Serializable {
          /** The ID of the vertex. */
          private final JobVertexID id;
        
          /**
           * The IDs of all operators contained in this vertex.
           *
           * <p>The ID pairs are stored depth-first post-order; for the forking chain below the ID's would
           * be stored as [D, E, B, C, A]. A - B - D \ \ C E This is the same order that operators are
           * stored in the {@code StreamTask}.
           */
          private final List<OperatorIDPair> operatorIDs;
      
          /** List of produced data sets, one per writer. */
          private final ArrayList<IntermediateDataSet> results = new ArrayList<>();
        
          /** List of edges with incoming data. One per Reader. */
          private final ArrayList<JobEdge> inputs = new ArrayList<>();
        
          /** The list of factories for operator coordinators. */
          private final ArrayList<SerializedValue<OperatorCoordinator.Provider>> operatorCoordinators = new ArrayList<>();
          
          /** The class of the invokable. */
          private String invokableClassName;
      
          /** Indicates of this job vertex is stoppable or not. */
          private boolean isStoppable = false;
        
          /** Optionally, a source of input splits. */
          private InputSplitSource<?> inputSplitSource;
        
          /**
           * The name of the vertex. This will be shown in runtime logs and will be in the runtime
           * environment.
           */
          private String name;
        
          /**
           * Optionally, a sharing group that allows subtasks from different job vertices to run
           * concurrently in one slot.
           */
          @Nullable private SlotSharingGroup slotSharingGroup;
        
          /** The group inside which the vertex subtasks share slots. */
          @Nullable private CoLocationGroup coLocationGroup;
        
          /**
           * Optional, the name of the operator, such as 'Flat Map' or 'Join', to be included in the JSON
           * plan.
           */
          private String operatorName;
      
          /**
           * Optional, the description of the operator, like 'Hash Join', or 'Sorted Group Reduce', to be
           * included in the JSON plan.
           */
          private String operatorDescription;
        
          /** Optional, pretty name of the operator, to be displayed in the JSON plan. */
          private String operatorPrettyName;
        
          /**
           * Optional, the JSON for the optimizer properties of the operator result, to be included in the
           * JSON plan.
           */
          private String resultOptimizerProperties;
        
          /** The input dependency constraint to schedule this vertex. */
          private InputDependencyConstraint inputDependencyConstraint = InputDependencyConstraint.ANY;
      }
      ```