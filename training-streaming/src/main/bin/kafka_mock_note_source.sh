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
  --class com.winfred.streamming.kafka.NodeMessageMock \
  --jobmanager yarn-cluster \
  --yarnslots 1 \
  --yarnjobManagerMemory 1024 \
  --yarntaskManagerMemory 1024 \
  --parallelism 3 \
  --detached \
  --yarnname NodeMessageMock \
  --yarnqueue crowd \
  ${HOME_PATH}/lib/training-streaming.jar \
    --note-send-topic "note_send_test_v2" \
    --note-receipt-topic "note_receipt_test_v2" \
    --interval-min 2 \
    --interval-max 10
EOF
)

echo -e "CMD:\n${CMD}\n"

${CMD}
