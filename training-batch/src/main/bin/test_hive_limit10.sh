#!/bin/bash

HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

CMD=$(
  cat <<EOF
/usr/local/service/flink/bin/flink run \
  --class com.winfred.training.hive.FlinkHiveTest \
  --jobmanager yarn-cluster \
  --yarnslots 2 \
  --yarnjobManagerMemory 2048 \
  --yarntaskManagerMemory 2048 \
  --parallelism 2 \
  --detached  \
  --yarnname flink-hvie-test \
  ${HOME_PATH}/lib/training-batch.jar \
  --hive-config-dir /usr/local/service/hive/conf \
  --database-name flink \
  --table-name test
EOF
)

echo -e "\n${CMD}\n"

${CMD}
