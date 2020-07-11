#!/bin/bash

FLINK_HOME=/usr/local/service/flink
HOME_PATH=$(cd "$(dirname "$0")"; cd ..; pwd)

CMD=$(cat << EOF
${FLINK_HOME}/bin/flink run \
  --class com.winfred.streamming.ckafka.CKafkaMockSource \
  --jobmanager yarn-cluster \
  --yarncontainer 3 \
  --yarnslots 4 \
  --yarnjobManagerMemory 4048 \
  --yarntaskManagerMemory 8096 \
  --parallelism 12 \
  --yarnstreaming \
  --detached \
  --yarnname CKafkaMockSource \
  ${HOME_PATH}/lib/training-streaming.jar
EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}
