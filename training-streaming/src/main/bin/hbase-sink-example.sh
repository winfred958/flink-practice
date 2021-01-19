#!/bin/bash

FLINK_HOME=/usr/local/service/flink
HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

CMD=$(
  cat <<EOF
${FLINK_HOME}/bin/flink run \
  --class com.winfred.streamming.example.HbaseExample \
  --jobmanager yarn-cluster \
  --yarnslots 1 \
  --yarnjobManagerMemory 2048 \
  --yarntaskManagerMemory 2048 \
  --parallelism 3 \
  --detached \
  --yarnname HbaseExample \
  ${HOME_PATH}/lib/training-streaming.jar \
    --zookeeper-quorum 10.1.2.4,10.1.2.2,10.1.2.26
EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}
