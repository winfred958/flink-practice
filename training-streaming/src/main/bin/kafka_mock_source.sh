#!/bin/bash

FLINK_HOME=/data/kai/flink
HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

source ~/.bash_profile

CMD=$(
  cat <<EOF
${FLINK_HOME}/bin/flink run \
  --class com.winfred.streamming.kafka.KafkaMockSource \
  --jobmanager yarn-cluster \
  --yarnslots 1 \
  --yarnjobManagerMemory 1024 \
  --yarntaskManagerMemory 1024 \
  --parallelism 3 \
  --detached \
  --yarnname KafkaMockSource \
  --yarnqueue default \
  ${HOME_PATH}/lib/training-streaming.jar \
  --topic-name kafka_test_raw \
  --interval-min 100 \
  --interval-max 500
EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}