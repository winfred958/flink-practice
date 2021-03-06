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
  --class com.winfred.streamming.ckafka.CKafkaExample \
  --jobmanager yarn-cluster \
  --yarnslots 4 \
  --yarnjobManagerMemory 4096 \
  --yarntaskManagerMemory 4096 \
  --parallelism 12 \
  --detached \
  --yarnname CKafkaExample \
  ${HOME_PATH}/lib/training-streaming.jar \
  --source-topic kafka_test_raw \
  --sink-topic kafka_test_target
EOF
)
# parallelism 12 ~~~ 吞吐 2900000/min
echo -e "CMD:\n${CMD}\n"

${CMD}
