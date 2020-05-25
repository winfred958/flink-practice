#!/bin/bash

HOME_PATH=$(cd "$(dirname "$0")"; cd ..; pwd)

CMD=$(cat << EOF
/usr/local/service/flink/bin/flink run \
  --class com.winfred.training.wc.WordCount \
  --jobmanager yarn-cluster \
  --yarncontainer 3 \
  --yarnslots 4 \
  --yarnjobManagerMemory 2048 \
  --yarntaskManagerMemory 2048 \
  --parallelism 12 \
  --detached  \
  --yarnname wordcount-test \
  ${HOME_PATH}/lib/training-batch.jar
EOF
)

echo -e "${CMD}"


