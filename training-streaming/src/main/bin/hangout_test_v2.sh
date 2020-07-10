#!/bin/bash

FLINK_HOME=/usr/local/service/flink


CMD=$(cat << EOF
${FLINK_HOME}/bin/flink run \
  --class com.tencent.pcg.main.FlinkHangout \
  --jobmanager yarn-cluster \
  --yarncontainer 3 \
  --yarnslots 4 \
  --yarnjobManagerMemory 4048 \
  --yarntaskManagerMemory 8096 \
  --parallelism 12 \
  --yarnstreaming \
  --detached \
  --yarnname FlinkHangoutTest \
  /data/home/itil/lib/flink_hangout-1.0-SNAPSHOT-jar-with-dependencies.jar 4512 146
EOF
)

echo -e "CMD:\n${CMD}"

${CMD}
