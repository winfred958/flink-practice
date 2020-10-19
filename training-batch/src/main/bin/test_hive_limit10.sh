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
  --hive-config-dir /usr/local/service/flink/hadoop_config \
  --catalog-name test_catalog \
  --database-name default

EOF
)

echo -e "\n${CMD}\n"

${CMD}
