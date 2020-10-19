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
  --class com.winfred.streamming.ckafka.CKafkaMockSource \
  --jobmanager yarn-cluster \
  --yarnslots 2 \
  --yarnjobManagerMemory 2048 \
  --yarntaskManagerMemory 2048 \
  --parallelism 6 \
  --detached \
  --yarnname CKafkaMockSource \
  ${HOME_PATH}/lib/training-streaming.jar
EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}
