# 目的, flink task 调度到指定的 node
## 1. [yarn node label](https://hadoop.apache.org/docs/r2.8.5/hadoop-yarn/hadoop-yarn-site/NodeLabel.html)

### 1.1 概述
- 1个node只能有存在于1个 node partition (label), 因此可以将集群中的node根据node partition划分到不相交的sub cluster; 默认所有node 都属于 <DEFAULT_PARTITION> (partition="")
- 用户需要配置每个 node partition (label)需要多少资源被不同的queue访问;
- 有2中 node partition
  - Exclusive: container allocation 必须 match node partition (partition="")
  - Non-exclusive: if a partition is non-exclusive, it shares idle resource to container requesting DEFAULT partition.
  
### 1.2 node label 所支持的功能
- partition node
  - 每个 node 被分配一个 label, 因此 cluster 资源将被分配为不相交的分区
- ACL of node label on queues
  - 用户可以指定 queue 能访问的 node label, 由此实现特定的node只能被特定的queue访问
- 指定1个partition可以访问的资源百分比
- 在资源请求中指定所需的节点标签
- 可操作性
  - Node label mapping 能被持久化, 在RM重启时恢复
  - Node label mapping 能很方便的修改, 
  - label on queue access 可以很方便的修改
- 可以通过三种方式将NM映射到节点标签
  - **Centralized**: 默认, RM exposed CLI, REST or RPC. 都可以使用
  - **Distributed**: 放到配置文件中
  - **Delegated-Centralized**: 复杂
  
### 1.3 配置方式
- 1.3.1 **Resource Manager 开启 Node label**
  - 修改配置文件 yarn-site.xml
    - yarn.node-labels.fs-store.root-dir =
        - 注意: 确保yarn进程有创建目录权限或确保目录存在并且有写权限 
    - yarn.node-labels.enabled = true
    - yarn.node-labels.configuration-type = **centralized**
- 1.3.2 **Add/modify node labels**
  - add node label
    - yarn rmadmin -addToClusterNodeLabels "destroy_node(exclusive=true)"
  - 查看集群label 列表
    - yarn cluster --list-node-labels
- 1.3.3 **Add/modify node-label mapping**
  - **Centralized** 模式
    - 添加 node -> label mapping
      - yarn rmadmin -replaceLabelsOnNode “node1-host=destroy_node node2-host=destroy_node”
    - 查看节点label
      - yarn node -status <NodeId>
  - **Distributed** 模式
    - 略
  - **Delegated-Centralized** 模式
    - 略
- 1.3.4 **Configuration of Schedulers for node labels**
  - 修改 capacity-scheduler.xml (EMR控制台)
    - ```xml
      <?xml version="1.0" encoding="UTF-8"?>
      <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
      <configuration>
      
        <property>
          <name>yarn.scheduler.capacity.maximum-am-resource-percent</name>
          <value>0.1</value>
        </property>
      
        <property>
          <name>yarn.scheduler.capacity.maximum-applications</name>
          <value>1000</value>
        </property>
      
        <!-- 划分2队列 root.default root.destroy -->
        <property>
          <name>yarn.scheduler.capacity.root.queues</name>
          <value>default,destroy</value>
        </property>
      
        <property>
          <name>yarn.scheduler.capacity.root.default.capacity</name>
          <value>90</value>
        </property>
      
        <property>
          <name>yarn.scheduler.capacity.root.destroy.capacity</name>
          <value>10</value>
        </property>
      
        <!-- 配置哪些标签可以被队列访问 -->
        <property>
          <name>yarn.scheduler.capacity.root.accessible-node-labels</name>
          <value>*</value>
          <description>root queue 可以访问所有 label, 否则, Accessible node labels for root queue will be ignored, it will be
            automatically set to "*".
          </description>
        </property>
      
        <property>
          <name>yarn.scheduler.capacity.root.default.accessible-node-labels</name>
          <value></value>
          <description>无标签的节点, 可以被 root.default 访问</description>
        </property>
      
        <property>
          <name>yarn.scheduler.capacity.root.destroy.accessible-node-labels</name>
          <value>destroy_node</value>
          <description>destroy_node标签的节点, 可以被 root.destroy 队列访问</description>
        </property>
      
        <!-- 队列所占用标签的容量 -->
        <property>
          <name>yarn.scheduler.capacity.root.default.accessible-node-labels..capacity</name>
          <value>100</value>
          <description>标签 的节点, 可以被 root.default 队列访访问, 可以使用的容量, </description>
        </property>
        <property>
          <name>yarn.scheduler.capacity.root.destroy.accessible-node-labels..capacity</name>
          <value>0</value>
          <description>标签 的节点, 可以被 root.destroy 队列访访问, 可以使用的容量</description>
        </property>
      
        <!-- 队列所占用标签的容量 -->
        <property>
          <name>yarn.scheduler.capacity.root.default.accessible-node-labels.destroy_node.capacity</name>
          <value>0</value>
          <description>标签 destroy_node 的节点, 可以被 root.default 队列访访问, 可以使用的容量, </description>
        </property>
        <property>
          <name>yarn.scheduler.capacity.root.destroy.accessible-node-labels.destroy_node.capacity</name>
          <value>100</value>
          <description>标签 destroy_node 的节点, 可以被 root.destroy 队列访访问, 可以使用的容量</description>
        </property>
      
      </configuration>
      ```