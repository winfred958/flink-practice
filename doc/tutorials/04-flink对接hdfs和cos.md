# Flink 写 hdfs/cos
## flink 写 cos
### [来源: Shaded Hadoop COS FileSystem](https://github.com/yuyang733/flink-cos-fs)
flink-cos-fs-hadoop 是COS对Flink文件系统的实现，并且支持了flink的recoverwriter接口。你可以按照如下步骤来使用它：

1. 执行`mkdir ${FLINK_HOME}/plugins/cos-fs-hadoop`，在`${FLINK_HOME}/plugins`目录下创建flink-cos-fs-hadoop插件目录；
2. 将对应版本的`flink-cos-fs-hadoop.jar`拷贝到`${FLINK_HOME}/plugins/cos-fs-hadoop`目录下；
3. 在${FLINK_HOME}/conf/flink-conf.yaml中添加一些COSN相关配置以确保flink能够访问到COS存储桶，这里的配置键与COSN完全兼容，可参考[hadoop-cos:[对象存储 Hadoop 工具 - 工具指南 - 文档中心 - 腾讯云](https://cloud.tencent.com/document/product/436/6884)](https://cloud.tencent.com/document/product/436/6884)，必须配置信息如下：

```yaml
fs.cosn.impl: org.apache.hadoop.fs.CosFileSystem
fs.AbstractFileSystem.cosn.impl: org.apache.hadoop.fs.CosN
fs.cosn.userinfo.secretId: AKIDXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
fs.cosn.userinfo.secretKey: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
fs.cosn.bucket.region: ap-guangzhou
fs.cosn.bucket.endpoint_suffix: cos.ap-guangzhou.myqcloud.com
```

**NOTE**：其他配置选项可参考上述链接，这些配置选项只用于自定义临时目录或性能调优使用，为可选项。

4. 在作业的write或sink路径中填写格式为：```cosn://bucket-appid/path```的路径信息即可，例如：

```java
        ...
        StreamingFileSink<String> fileSink  =  StreamingFileSink.forRowFormat(
                new Path(parameterTool.get("cosn://flink-test-1250000000/sink-test")),
                new SimpleStringEncoder<String>("UTF-8"))
                .build();
        ...
```
