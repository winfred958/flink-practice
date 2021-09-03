# flink on yarn 详细流程

```
https://developer.aliyun.com/article/719262#
```

## 1. client 端流程 (flink-yarn)

### 1.1 构造 JobGraph

### 1.2 校验

- user权限
- isReadyForDeployment
    - jar 是否存在
    - flink-config.yaml 是否存在
    - VCore 够不够
- 队列
- 集群 Memory

### 1.3 环境遍量, jar, 准备

#### 1.3.1 初始化 FileSystem

#### 1.3.2 Add Zookeeper namespace to local flinkConfiguraton

- ha 模式

#### 1.3.3 上传各种依赖到 hdfs

- Register all files in provided lib dirs as local resources with public visibility and upload the remaining
  dependencies as local resources with APPLICATION visibility.
- upload and register ship-only files, lugin files only need to be shipped and should not be added to classpath.
- Upload and register user jars
- classpath assembler
- Setup jar for ApplicationMaster

#### 1.3.4 JobGraph 生成 job.graph 文件, 上传到指定目录

- ```java
  public class YarnClusterDescriptor implements ClusterDescriptor<ApplicationId> {
      private ApplicationReport startAppMaster(
            Configuration configuration,
            String applicationName,
            String yarnClusterEntrypoint,
            JobGraph jobGraph,
            YarnClient yarnClient,
            YarnClientApplication yarnApplication,
            ClusterSpecification clusterSpecification)
            throws Exception {
  
        // ... ... 
        
        // write job graph to tmp file and add it to local resource
        // TODO: server use user main method to generate job graph
        if (jobGraph != null) {
            File tmpJobGraphFile = null;
            try {
                tmpJobGraphFile = File.createTempFile(appId.toString(), null);
                try (FileOutputStream output = new FileOutputStream(tmpJobGraphFile);
                        ObjectOutputStream obOutput = new ObjectOutputStream(output)) {
                    obOutput.writeObject(jobGraph);
                }

                final String jobGraphFilename = "job.graph";
                configuration.setString(JOB_GRAPH_FILE_PATH, jobGraphFilename);

                fileUploader.registerSingleLocalResource(
                        jobGraphFilename,
                        new Path(tmpJobGraphFile.toURI()),
                        "",
                        LocalResourceType.FILE,
                        true,
                        false);
                classPathBuilder.append(jobGraphFilename).append(File.pathSeparator);
            } catch (Exception e) {
                LOG.warn("Add job graph to local resource fail.");
                throw e;
            } finally {
                if (tmpJobGraphFile != null && !tmpJobGraphFile.delete()) {
                    LOG.warn("Fail to delete temporary file {}.", tmpJobGraphFile.toPath());
                }
            }
        }
      // ... ... 
      }
  }
  ```

#### 1.3.5 构造 ApplicationSubmissionContext, 设置 CLASSPATH 和 ENV

### 1.4 client 端 提交 job

- ```text
  yarnClient.submitApplication(appContext);
  ```

## 2. ResourceManager 流程 (yarn: hadoop-yarn-server-resourcemanager)

### 2.1 ClientRMService

### 2.2 RMAppManager

### 2.3 RMAppImpl