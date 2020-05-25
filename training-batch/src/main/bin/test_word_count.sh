#!/bin/bash

HOME_PATH=$(cd "$(dirname "$0")"; cd ..; pwd)

CMD=$(cat << EOF
/usr/local/service/flink/binf/flink run \
  --class com.winfred.training.wordcount.WordCount \
  --jobmanager yarn-cluster \
  --yarncontainer 3 \
  --yarnslots 4 \
  --yarnjobManagerMemory 2048 \
  --yarntaskManagerMemory 2048 \
  --parallelism 12 \
  --yarnstreaming \
  --detached  \
  --yarnname wordcount-test \
  "${HOME_PATH}/lib/training-batch.jar"
EOF
)

echo -e "${CMD}"


