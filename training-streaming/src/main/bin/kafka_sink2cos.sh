#!/bin/bash

FLINK_HOME=/usr/local/service/flink
HOME_PATH=$(cd "$(dirname "$0")"; cd ..; pwd)

CMD=$(cat << EOF
${FLINK_HOME}/bin/flink run \
  --class com.winfred.streamming.ckafka.CKafkaExample \
  --jobmanager yarn-cluster \
  --yarnslots 4 \
  --yarnjobManagerMemory 4096 \
  --yarntaskManagerMemory 4096 \
  --parallelism 12 \
  --detached \
  --yarnname CKafkaExample \
  ${HOME_PATH}/lib/training-streaming.jar \
    --target-path "cosn://emr-streamming-test-1258469122/tmp/parquet-streamming-sink"
EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}
