#!/bin/bash

HOME_PATH=$(
  cd "$(dirname "$0")"
  cd ..
  pwd
)

CMD=$(
  cat <<EOF
/usr/local/service/flink/bin/flink run \
  --class com.winfred.training.wc.WordCount \
  --jobmanager yarn-cluster \
  --yarnslots 4 \
  --yarnjobManagerMemory 2048 \
  --yarntaskManagerMemory 2048 \
  --parallelism 12 \
  --detached  \
  --yarnname wordcount-test \
  ${HOME_PATH}/lib/training-batch.jar --input-path hdfs:///usr/hadoop/tmp/test/blog-context.text
EOF
)

echo -e "\n${CMD}\n"

${CMD}
